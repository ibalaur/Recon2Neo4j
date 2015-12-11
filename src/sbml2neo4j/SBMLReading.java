package sbml2neo4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ontology.Term;

import sbml2neo4j.Recon2Graph.RelTypes;

public class SBMLReading {

	private final String RECON2_XML_FILE = "files/recon2.v02.xml";

	Map<String, Node> speciesHashMap = new HashMap<String, Node>();
	Map<String, Node> reactionHashMap = new HashMap<String, Node>();
	List<Node> compoundSpeciesList = new ArrayList<Node>();
	List<Relationship> vRelationsArray = new ArrayList<Relationship>();
	ListOf<Compartment> _listCompartment;

	@SuppressWarnings("deprecation")
	public void readXMLFile() throws IOException {
		try {
			Model model = null;
			SBMLReader reader = new SBMLReader();
			SBMLDocument doc = null;

			doc = reader.readSBML(RECON2_XML_FILE);
			model = doc.getModel();

			_listCompartment = model.getListOfCompartments();

			for (Iterator<Species> iterator = model.getListOfSpecies()
					.iterator(); iterator.hasNext();) {
				Species species = (Species) iterator.next();

				Annotation _annotation = species.getAnnotation();

				List<CVTerm> cvtList = _annotation.getListOfCVTerms();
				getIdentifiersList(cvtList);

				String szIdentifiers = getIdentifiersList(cvtList);

				String szUniprotIdFullString = "";
				List<String> szUniprotIdList = new ArrayList<String>();

				if (!szIdentifiers.isEmpty()) {
					if ((szIdentifiers.contains("uniprot"))
							|| (szIdentifiers.contains("UNIPROT"))) {
						String findStr = "http://identifiers.org/uniprot/";
						int lastIndex = 0;

						while (lastIndex != -1) {

							lastIndex = szIdentifiers.indexOf(findStr,
									lastIndex);

							if (lastIndex != -1) {
								String szUniprotId = szIdentifiers.substring(
										lastIndex + findStr.length(), lastIndex
												+ findStr.length() + 6);
								szUniprotIdFullString = szUniprotIdFullString
										+ szUniprotId + " ";
								lastIndex += findStr.length();

								szUniprotIdList.add(szUniprotId);
							}
						}
					}
				}

				Recon2Graph.LabelTypes speciesType = mappingSBOTerm2NodeType(species
						.getSBOTermID());
				// add species to neo4j graph
				if (Recon2Graph.LabelTypes.Metabolite == speciesType) {
					speciesHashMap.put(
							species.getId(),
							Recon2Graph.getGraphInstance().createNode(
									Recon2Graph.LabelTypes.Metabolite));
					speciesHashMap.get(species.getId()).setProperty(
							"MetaboliteId", species.getId().trim());
					speciesHashMap.get(species.getId()).setProperty(
							"MetaboliteName", species.getName().trim());
				} else if (Recon2Graph.LabelTypes.Protein == speciesType) {
					speciesHashMap.put(
							species.getId(),
							Recon2Graph.getGraphInstance().createNode(
									Recon2Graph.LabelTypes.Protein));
					speciesHashMap.get(species.getId()).setProperty(
							"ProteinId", species.getId().trim());
					speciesHashMap.get(species.getId()).setProperty(
							"ProteinName", species.getName().trim());
					speciesHashMap.get(species.getId()).setProperty(
							"Identifiers", szIdentifiers);
					speciesHashMap.get(species.getId()).setProperty(
							"UNIPROT_ID", szUniprotIdFullString);
				} else if (Recon2Graph.LabelTypes.Complex == speciesType) {
					speciesHashMap.put(
							species.getId(),
							Recon2Graph.getGraphInstance().createNode(
									Recon2Graph.LabelTypes.Complex));
					speciesHashMap.get(species.getId()).setProperty(
							"ComplexId", species.getId().trim());
					speciesHashMap.get(species.getId()).setProperty(
							"ComplexName", species.getName().trim());
					speciesHashMap.get(species.getId()).setProperty(
							"Identifiers", szIdentifiers);
					speciesHashMap.get(species.getId()).setProperty(
							"UNIPROT_ID", szUniprotIdFullString);

					for (String eUniprotIdStr : szUniprotIdList) {
						Node _node = Recon2Graph.getGraphInstance().createNode(
								Recon2Graph.LabelTypes.ProteinCompound);
						_node.setProperty("UNIPROT_ID", eUniprotIdStr);
						_node.createRelationshipTo(
								speciesHashMap.get(species.getId()),
								RelTypes.Part_Of);
					}
				}

				speciesHashMap.get(species.getId()).setProperty(
						"initialConcentration",
						String.valueOf(species.getInitialConcentration()));
				speciesHashMap.get(species.getId()).setProperty("isConstant",
						String.valueOf(species.getConstant()));
				speciesHashMap.get(species.getId()).setProperty(
						"boundaryConditions",
						String.valueOf(species.getBoundaryCondition()));
				speciesHashMap.get(species.getId()).setProperty(
						"hasOnlySubstanceUnits",
						String.valueOf(species.getHasOnlySubstanceUnits()));
				speciesHashMap.get(species.getId()).setProperty("charge",
						String.valueOf(species.getCharge()));
				speciesHashMap.get(species.getId()).setProperty("SBOTermId",
						species.getSBOTermID());

				speciesHashMap.get(species.getId()).setProperty("metadata",
						species.getMetaId());
				speciesHashMap.get(species.getId()).setProperty("SpeciesNotes",
						species.getNotesString());

				mappingCompartmentInfo(species.getId(),
						species.getCompartment());
			}

			for (Iterator<Reaction> iterator = model.getListOfReactions()
					.iterator(); iterator.hasNext();) {

				Reaction reaction = (Reaction) iterator.next();

				reactionHashMap.put(
						reaction.getId(),
						Recon2Graph.getGraphInstance().createNode(
								Recon2Graph.LabelTypes.Reaction));
				reactionHashMap.get(reaction.getId()).setProperty("ReactionId",
						reaction.getId().trim());
				reactionHashMap.get(reaction.getId()).setProperty(
						"ReactionName", reaction.getName().trim());
				reactionHashMap.get(reaction.getId()).setProperty(
						"ReactionMetadataId", reaction.getMetaId().trim());
				reactionHashMap.get(reaction.getId()).setProperty(
						"ReactionReversible", reaction.getReversible());
				reactionHashMap.get(reaction.getId()).setProperty("SBOTermId",
						reaction.getSBOTermID());
				reactionHashMap.get(reaction.getId()).setProperty(
						"ReactionNotes", reaction.getNotesString());
				reactionHashMap.get(reaction.getId()).setProperty(
						"KineticLaw_Math",
						reaction.getKineticLaw().getMath().toString());

				// link species used as modifier to reaction
				ListOf<ModifierSpeciesReference> lom = reaction
						.getListOfModifiers();
				for (Iterator<ModifierSpeciesReference> itLom = lom.iterator(); itLom
						.hasNext();) {
					ModifierSpeciesReference msr = (ModifierSpeciesReference) itLom
							.next();

					for (Entry<String, Node> eSpecies : speciesHashMap
							.entrySet()) {
						if (eSpecies.getKey().equals(msr.getSpecies())) {
							vRelationsArray.add(eSpecies.getValue()
									.createRelationshipTo(
											reactionHashMap.get(reaction
													.getId()),
											RelTypes.Catalysis));
						}
					}
				}

				// link species used as reactant to reaction
				ListOf<SpeciesReference> lor = reaction.getListOfReactants();
				for (Iterator<SpeciesReference> itLor = lor.iterator(); itLor
						.hasNext();) {
					SpeciesReference msr = (SpeciesReference) itLor.next();

					for (Entry<String, Node> eSpecies : speciesHashMap
							.entrySet()) {
						if (eSpecies.getKey().equals(msr.getSpecies())) {
							vRelationsArray.add(eSpecies.getValue()
									.createRelationshipTo(
											reactionHashMap.get(reaction
													.getId()),
											RelTypes.Consumption));
							vRelationsArray.get(vRelationsArray.size() - 1)
									.setProperty("Stoichiometry",
											msr.getStoichiometry());
						}
					}
				}

				// link species used as product to reaction
				ListOf<SpeciesReference> lop = reaction.getListOfProducts();
				for (Iterator<SpeciesReference> itLop = lop.iterator(); itLop
						.hasNext();) {
					SpeciesReference msr = (SpeciesReference) itLop.next();

					for (Entry<String, Node> eSpecies : speciesHashMap
							.entrySet()) {
						if (eSpecies.getKey().equals(msr.getSpecies())) {
							vRelationsArray.add(reactionHashMap.get(
									reaction.getId()).createRelationshipTo(
									eSpecies.getValue(), RelTypes.Production));
							vRelationsArray.get(vRelationsArray.size() - 1)
									.setProperty("Stoichiometry",
											msr.getStoichiometry());
						}
					}
				}

				ListOf<LocalParameter> loLocParam = reaction.getKineticLaw()
						.getListOfLocalParameters();
				for (Iterator<LocalParameter> itLoLocParam = loLocParam
						.iterator(); itLoLocParam.hasNext();) {
					LocalParameter msr = (LocalParameter) itLoLocParam.next();
					reactionHashMap.get(reaction.getId()).setProperty(
							msr.getId(),
							String.valueOf(msr.getValue()) + " "
									+ msr.getUnits());
				}
			}

			int proteinNo = 0, metaboliteNo = 0, complexNo = 0;
			for (Entry<String, Node> eSpecies : speciesHashMap.entrySet()) {
				Iterator<org.neo4j.graphdb.Label> it = eSpecies.getValue()
						.getLabels().iterator();
				String szSpeciesLabel = it.next().toString();

				if (szSpeciesLabel.equals(Recon2Graph.LabelTypes.Protein
						.toString())) {
					proteinNo++;
				}
				if (szSpeciesLabel.equals(Recon2Graph.LabelTypes.Metabolite
						.toString())) {
					metaboliteNo++;
				}
				if (szSpeciesLabel.equals(Recon2Graph.LabelTypes.Complex
						.toString())) {
					complexNo++;
				}
			}

			int nConsumption = 0, nProduction = 0, nCatalysis = 0;
			int nConsumptionProtein = 0, nConsumptionMetabolite = 0, nConsumptionComplex = 0, nCatalysisProtein = 0, nCatalysisMetabolite = 0, nCatalysisComplex = 0, nProductionProtein = 0, nProductionMetabolite = 0, nProductionComplex = 0;
			for (int i = 0; i < vRelationsArray.size(); i++) {
				String szStartNodeLabel = vRelationsArray.get(i).getStartNode()
						.getLabels().iterator().next().toString();
				String szEndNodeLabel = vRelationsArray.get(i).getEndNode()
						.getLabels().iterator().next().toString();

				if (vRelationsArray.get(i).getType().name()
						.equals(RelTypes.Consumption.toString())) {

					if (szStartNodeLabel.equals(Recon2Graph.LabelTypes.Protein
							.toString())) {
						nConsumptionProtein++;
					}
					if (szStartNodeLabel
							.equals(Recon2Graph.LabelTypes.Metabolite
									.toString())) {
						nConsumptionMetabolite++;
					}
					if (szStartNodeLabel.equals(Recon2Graph.LabelTypes.Complex
							.toString())) {
						nConsumptionComplex++;
					}
					nConsumption++;
				}
				if (vRelationsArray.get(i).getType().name()
						.equals(RelTypes.Production.toString())) {
					if (szEndNodeLabel.equals(Recon2Graph.LabelTypes.Protein
							.toString())) {
						nProductionProtein++;
					}
					if (szEndNodeLabel.equals(Recon2Graph.LabelTypes.Metabolite
							.toString())) {
						nProductionMetabolite++;
					}
					if (szEndNodeLabel.equals(Recon2Graph.LabelTypes.Complex
							.toString())) {
						nProductionComplex++;
					}
					nProduction++;
				}

				if (vRelationsArray.get(i).getType().name()
						.equals(RelTypes.Catalysis.toString())) {
					if (szStartNodeLabel.equals(Recon2Graph.LabelTypes.Protein
							.toString())) {
						nCatalysisProtein++;
					}
					if (szStartNodeLabel
							.equals(Recon2Graph.LabelTypes.Metabolite
									.toString())) {
						nCatalysisMetabolite++;
					}
					if (szStartNodeLabel.equals(Recon2Graph.LabelTypes.Complex
							.toString())) {
						nCatalysisComplex++;
					}
					nCatalysis++;
				}
			}

			System.out
					.printf("Species number =%d\n Reactions = %d\n Proteins =%d\n Metabolites = %d\n Complex = %d\n TotalConsumption =%d\n TotalProduction=%d\n TotalCatalysis=%d\n ProteinConsumption=%d\n ProteinProduction=%d\n ProteinCatalysis=%d\n MetaboliteConsumption=%d\n MetaboliteProduction=%d\n MetaboliteCatalysis=%d\n ComplexConsumption=%d\n ComplexProduction=%d\n ComplexCatalysis=%d\n ",
							speciesHashMap.size(), reactionHashMap.size(),
							proteinNo, metaboliteNo, complexNo, nConsumption,
							nProduction, nCatalysis, nConsumptionProtein,
							nProductionProtein, nCatalysisProtein,
							nConsumptionMetabolite, nProductionMetabolite,
							nCatalysisMetabolite, nConsumptionComplex,
							nProductionComplex, nCatalysisComplex);
		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getIdentifiersList(List<CVTerm> cvtList) {
		String szIdentifiersList = "";

		for (Iterator<CVTerm> cvtIt = cvtList.iterator(); cvtIt.hasNext();) {
			CVTerm cvTerm = (CVTerm) cvtIt.next();

			// identify the qualifier
			if (cvTerm.isBiologicalQualifier()) {
				// get the resources (URI)
				szIdentifiersList = szIdentifiersList + " "
						+ cvTerm.getResources().toString();

			}
		}
		return szIdentifiersList;

	}

	private void mappingCompartmentInfo(String SpeciesId, String szShortCompName) {

		for (Iterator<Compartment> iterator = _listCompartment.iterator(); iterator
				.hasNext();) {
			Compartment _comp = (Compartment) iterator.next();
			if (_comp.getId().equals(szShortCompName)) {

				speciesHashMap.get(SpeciesId).setProperty("CompartId",
						_comp.getId());
				speciesHashMap.get(SpeciesId).setProperty("CompartName",
						_comp.getName());
				speciesHashMap.get(SpeciesId).setProperty("CompartConstant",
						_comp.getConstant());
				speciesHashMap.get(SpeciesId).setProperty(
						"CompartSpatialDimensions",
						_comp.getSpatialDimensions());
				speciesHashMap.get(SpeciesId).setProperty("CompartMetaId",
						_comp.getMetaId());
				speciesHashMap.get(SpeciesId).setProperty("CompartSBOTermID",
						_comp.getSBOTermID());
				speciesHashMap.get(SpeciesId).setProperty("CompartSize",
						_comp.getSize());
			}
		}

	}

	private Recon2Graph.LabelTypes mappingSBOTerm2NodeType(String szSBOTermIdStr) {
		Term _term = org.sbml.jsbml.SBO.getTerm(szSBOTermIdStr);
		Recon2Graph.LabelTypes speciesLabel = Recon2Graph.LabelTypes.Protein;
		if (_term.getName().trim().equals("polypeptide chain")) {
			speciesLabel = Recon2Graph.LabelTypes.Protein;
		} else if (_term.getName().trim().equals("protein complex")) {
			speciesLabel = Recon2Graph.LabelTypes.Complex;
		} else if (_term.getName().trim().equals("simple chemical")) {
			speciesLabel = Recon2Graph.LabelTypes.Metabolite;
		} else {
			System.out.println(szSBOTermIdStr + " " + _term.toString());
		}

		return speciesLabel;
	}
}
