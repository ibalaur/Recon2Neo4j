package parserCode;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.neo4j.graphdb.Label;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Creator;
import org.sbml.jsbml.History;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.UnitDefinition;

import java.util.HashMap;
import java.util.Map.Entry;

public class JSON2SBMLParser {

	public static enum LabelTypes implements Label {
		Reaction, Protein, Metabolite, Complex, ProteinCompound
	}

	// Will create the History, if it does not exist
	Creator creator = new Creator("EISBM", "EISBM", "EISBM",
			"ibalaur@eisbm.org");
	SBMLDocument doc = new SBMLDocument(2, 4);

	// Create a new SBML model, and add a compartment to it.
	Model model = doc.createModel("recon2_subnetwork");

	// Create a model history object and add author information to it.
	History hist = model.getHistory();
	List<String> _ListOfCompartments = new ArrayList<String>();
	Map<String, Reaction> _ListOfReactions = new HashMap<String, Reaction>();
	Map<String, Species> _ListOfSpecies = new HashMap<String, Species>();
	List<String> _ListOfUnitDefinitions = new ArrayList<String>();
	Map<String, ReactionSpecies> _ListOfReactionSpecies = new HashMap<String, ReactionSpecies>();

	public void parseJSONString(String szInFileName)
			throws FileNotFoundException, IOException, ParseException {
		JSONParser jsonParser = new JSONParser();

		JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader(
				szInFileName));

		Object obj = jsonObject.get("data");

		if (obj instanceof JSONArray) {
			JSONArray dataArray = (JSONArray) obj;
			for (int i = 0; i < dataArray.size(); i++) {

				if (dataArray.get(i) instanceof JSONObject) {

					Object obj1 = ((JSONObject) dataArray.get(i)).get("graph");

					if (obj1 instanceof JSONObject) {
						JSONObject graph = (JSONObject) obj1;
						Object nodes = graph.get("nodes");
						Object relationships = graph.get("relationships");
						parseNodes(nodes);
						parseRelationships(relationships);
					} else {
						System.out.println("error - graph");
					}
				} else {
					System.out.println("error - data element");
				}
			}
		} else {
			System.out.println("Data array - error");
		}
	}

	public void createOutputSBMLFile(String szSBMLOutputFileName,
			String szLogFileName) {
		hist.addCreator(creator);

		try {
			SBMLWriter.write(doc, szSBMLOutputFileName, "Neo4jParser", "1.0");
		} catch (SBMLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		doc.checkConsistency();

		if (null != szLogFileName) {
			PrintStream writeLogs;
			try {
				writeLogs = new PrintStream(new FileOutputStream(szLogFileName));
				if (doc.getErrorCount() > 0) {
					doc.printErrors(writeLogs);
				} else {
					writeLogs.println("SBML file consistency check: No errors");
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void parseRelationships(Object relationships) {
		JSONArray _relArray = (JSONArray) relationships;

		for (int i = 0; i < _relArray.size(); i++) {
			JSONObject _rel = (JSONObject) _relArray.get(i);
			String strStartNodeId = _rel.get("startNode").toString();
			String strEndNodeId = _rel.get("endNode").toString();
			String strType = _rel.get("type").toString();

			if (!strType.equals("Part_Of")) {
				if (strType.equals("Catalysis")) {
					String szReaction = _ListOfReactions.get(strEndNodeId)
							.getId();
					if (!_ListOfReactionSpecies.containsKey(szReaction)) {
						_ListOfReactionSpecies.put(szReaction,
								new ReactionSpecies());
					}

					if (!_ListOfReactionSpecies.get(szReaction)._listOfModifiers
							.contains(_ListOfSpecies.get(strStartNodeId)
									.getId())) {

						_ListOfReactions.get(strEndNodeId).createModifier(
								_ListOfSpecies.get(strStartNodeId));

						_ListOfReactionSpecies.get(szReaction)._listOfModifiers
								.add(_ListOfSpecies.get(strStartNodeId).getId());
					}
				} else {
					JSONObject _prop = (JSONObject) _rel.get("properties");
					String strStoichiometry = _prop.get("Stoichiometry")
							.toString();
					double _stoichiometry = Double.valueOf(strStoichiometry);

					if (strType.equals("Consumption")) {

						String szReaction = _ListOfReactions.get(strEndNodeId)
								.getId();
						if (!_ListOfReactionSpecies.containsKey(szReaction)) {
							_ListOfReactionSpecies.put(szReaction,
									new ReactionSpecies());
						}

						if (!_ListOfReactionSpecies.get(szReaction)._listOfReactants
								.contains(_ListOfSpecies.get(strStartNodeId)
										.getId())) {
							SpeciesReference react = _ListOfReactions.get(
									strEndNodeId).createReactant(
									_ListOfSpecies.get(strStartNodeId));

							if (_stoichiometry > 1) {
								react.setStoichiometry(_stoichiometry);
							}
							_ListOfReactionSpecies.get(szReaction)._listOfReactants
									.add(_ListOfSpecies.get(strStartNodeId)
											.getId());
						}
					} else if (strType.equals("Production")) {

						String szReaction = _ListOfReactions
								.get(strStartNodeId).getId();
						if (!_ListOfReactionSpecies.containsKey(szReaction)) {
							_ListOfReactionSpecies.put(szReaction,
									new ReactionSpecies());
						}

						if (!_ListOfReactionSpecies.get(szReaction)._listOfProducts
								.contains(_ListOfSpecies.get(strEndNodeId)
										.getId())) {

							SpeciesReference prod = _ListOfReactions.get(
									strStartNodeId).createProduct(
									_ListOfSpecies.get(strEndNodeId));
							if (_stoichiometry > 1) {
								prod.setStoichiometry(_stoichiometry);
							}

							_ListOfReactionSpecies.get(szReaction)._listOfProducts
									.add(_ListOfSpecies.get(strEndNodeId)
											.getId());
						}

					}
				}
			}
		}
	}

	private void parseNodes(Object nodes) {
		if (nodes instanceof JSONArray) {
			JSONArray _nodeArray = (JSONArray) nodes;

			for (int i = 0; i < _nodeArray.size(); i++) {
				Object _NodeObject = ((JSONArray) nodes).get(i);

				if (_NodeObject instanceof JSONObject) {
					parseNode(_NodeObject);
				} else {
					System.out.println("Error - _NodeObject");
				}
			}
		} else {
			System.out.println("Error: nodes");
		}
	}

	private void parseNode(Object _Object) {
		JSONObject _Node = (JSONObject) _Object;
		JSONArray _labelArray = (JSONArray) _Node.get("labels");

		LabelTypes _nodeLabel = mapLabel(_labelArray.get(0).toString());

		if (LabelTypes.Protein == _nodeLabel) {
			parseProteinInfo(_Node);
		} else if (LabelTypes.Reaction == _nodeLabel) {
			parseReactionInfo(_Node);
		} else if (LabelTypes.Complex == _nodeLabel) {
			parseComplexInfo(_Node);
		} else if (LabelTypes.Metabolite == _nodeLabel) {
			parseMetaboliteInfo(_Node);
		}
	}

	private void parseMetaboliteInfo(JSONObject _node) {
		JSONObject _prop = (JSONObject) _node.get("properties");
		String strMetaboliteId = _prop.get("MetaboliteId").toString();

		String strMetaboliteName = _prop.get("MetaboliteName").toString();
		String strInitialConcentration = _prop.get("initialConcentration")
				.toString();
		String strIsConstant = _prop.get("isConstant").toString();
		String strBoundaryConditions = _prop.get("boundaryConditions")
				.toString();
		String strHasOnlySubstanceUnits = _prop.get("hasOnlySubstanceUnits")
				.toString();
		String strCharge = _prop.get("charge").toString();
		String strSBOTermId = _prop.get("SBOTermId").toString();
		String strMetadata = _prop.get("metadata").toString();
		String strCompartId = _prop.get("CompartId").toString();
		String strCompartName = _prop.get("CompartName").toString();
		String strCompartConstant = _prop.get("CompartConstant").toString();
		String strCompartSpatialDimensions = _prop.get(
				"CompartSpatialDimensions").toString();
		String strCompartMetaId = _prop.get("CompartMetaId").toString();
		String strCompartSBOTermID = _prop.get("CompartSBOTermID").toString();
		String strCompartSize = _prop.get("CompartSize").toString();
		// String szAnnotation = _prop.get("SpeciesAnnotation").toString();
		String szNote = _prop.get("SpeciesNotes").toString();

		// create SBML Species node

		if (!foundSpecies(strMetaboliteId)) {
			createSpecies(strMetaboliteId, strMetaboliteName,
					strInitialConcentration, strBoundaryConditions, strCharge,
					strIsConstant, strHasOnlySubstanceUnits, strSBOTermId,
					strMetadata, strCompartId, szNote, _node.get("id")
							.toString());
		}

		createCompartment(strCompartName, strCompartId, strCompartConstant,
				strCompartSpatialDimensions, strCompartMetaId,
				strCompartSBOTermID, strCompartSize);
	}

	private void parseProteinInfo(JSONObject _node) {
		// TODO Auto-generated method stub
		JSONObject _prop = (JSONObject) _node.get("properties");
		String strInitialConcentration = _prop.get("initialConcentration")
				.toString();
		String strIsConstant = _prop.get("isConstant").toString();
		String strBoundaryConditions = _prop.get("boundaryConditions")
				.toString();
		String strHasOnlySubstanceUnits = _prop.get("hasOnlySubstanceUnits")
				.toString();
		String strCharge = _prop.get("charge").toString();
		String strSBOTermId = _prop.get("SBOTermId").toString();
		String strMetadata = _prop.get("metadata").toString();
		String strProteinId = _prop.get("ProteinId").toString();
		String strProteinName = _prop.get("ProteinName").toString();

		String strCompartId = _prop.get("CompartId").toString();
		String strCompartName = _prop.get("CompartName").toString();
		String strCompartConstant = _prop.get("CompartConstant").toString();
		String strCompartSpatialDimensions = _prop.get(
				"CompartSpatialDimensions").toString();
		String strCompartMetaId = _prop.get("CompartMetaId").toString();
		String strCompartSBOTermID = _prop.get("CompartSBOTermID").toString();
		String strCompartSize = _prop.get("CompartSize").toString();
		String szNote = _prop.get("SpeciesNotes").toString();

		if (!foundSpecies(strProteinId)) {
			createSpecies(strProteinId, strProteinName,
					strInitialConcentration, strBoundaryConditions, strCharge,
					strIsConstant, strHasOnlySubstanceUnits, strSBOTermId,
					strMetadata, strCompartId, szNote, _node.get("id")
							.toString());
		}

		createCompartment(strCompartName, strCompartId, strCompartConstant,
				strCompartSpatialDimensions, strCompartMetaId,
				strCompartSBOTermID, strCompartSize);
	}

	private void createCompartment(String strCompartName, String _Id,
			String _Constant, String _SpatialDimension, String _MetaId,
			String _SBOTermId, String _Size) {

		if (!foundCompartment(strCompartName)) {
			Compartment _compartment = model.createCompartment(_Id);
			_compartment.setName(strCompartName);
			_compartment.setConstant(Boolean.valueOf(_Constant));
			_compartment
					.setSpatialDimensions(Double.valueOf(_SpatialDimension));
			_compartment.setMetaId(_MetaId);
			_compartment.setSBOTerm(_SBOTermId);
			_compartment.setSize(Double.valueOf(_Size));

			_ListOfCompartments.add(strCompartName);
		}
	}

	@SuppressWarnings("deprecation")
	private void createSpecies(String strSpeciesId, String strSpeciesName,
			String strInitialConcentration, String strBoundaryConditions,
			String strCharge, String strIsConstant,
			String strHasOnlySubstanceUnits, String strSBOTermId,
			String strMetadata, String strCompartId, String szNote,
			String strNodeId) {
		Species eSpecies = model.createSpecies(strSpeciesId);
		eSpecies.setName(strSpeciesName);
		eSpecies.setInitialConcentration(Double
				.parseDouble(strInitialConcentration));
		eSpecies.setBoundaryCondition(Boolean.valueOf(strBoundaryConditions));
		eSpecies.setCharge(Integer.parseInt(strCharge));
		eSpecies.setConstant(Boolean.valueOf(strIsConstant));
		eSpecies.setHasOnlySubstanceUnits(Boolean
				.valueOf(strHasOnlySubstanceUnits));
		try {
			eSpecies.setSBOTerm(strSBOTermId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		eSpecies.setMetaId(strMetadata);
		eSpecies.setCompartment(strCompartId);
		try {
			eSpecies.setNotes(szNote);
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		 * try { eSpecies.setAnnotation(new Annotation(szAnnotation)); } catch
		 * (XMLStreamException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */
		_ListOfSpecies.put(strNodeId, eSpecies);
	}

	private void parseComplexInfo(JSONObject _node) {
		// TODO Auto-generated method stub
		JSONObject _prop = (JSONObject) _node.get("properties");
		String strInitialConcentration = _prop.get("initialConcentration")
				.toString();
		String strIsConstant = _prop.get("isConstant").toString();
		String strBoundaryConditions = _prop.get("boundaryConditions")
				.toString();
		String strHasOnlySubstanceUnits = _prop.get("hasOnlySubstanceUnits")
				.toString();
		String strCharge = _prop.get("charge").toString();
		String strSBOTermId = _prop.get("SBOTermId").toString();
		String strMetadata = _prop.get("metadata").toString();
		String strComplexId = _prop.get("ComplexId").toString();
		String strComplexName = _prop.get("ComplexName").toString();

		String strCompartId = _prop.get("CompartId").toString();
		String strCompartName = _prop.get("CompartName").toString();
		String strCompartConstant = _prop.get("CompartConstant").toString();
		String strCompartSpatialDimensions = _prop.get(
				"CompartSpatialDimensions").toString();
		String strCompartMetaId = _prop.get("CompartMetaId").toString();
		String strCompartSBOTermID = _prop.get("CompartSBOTermID").toString();
		String strCompartSize = _prop.get("CompartSize").toString();
		String szNote = _prop.get("SpeciesNotes").toString();

		if (!foundSpecies(strComplexId)) {
			createSpecies(strComplexId, strComplexName,
					strInitialConcentration, strBoundaryConditions, strCharge,
					strIsConstant, strHasOnlySubstanceUnits, strSBOTermId,
					strMetadata, strCompartId, szNote, _node.get("id")
							.toString());
		}

		createCompartment(strCompartName, strCompartId, strCompartConstant,
				strCompartSpatialDimensions, strCompartMetaId,
				strCompartSBOTermID, strCompartSize);
	}

	private void parseReactionInfo(JSONObject _node) {
		// TODO Auto-generated method stub

		JSONObject _prop = (JSONObject) _node.get("properties");

		String strSBOTermId = _prop.get("SBOTermId").toString();
		String strReactionId = _prop.get("ReactionId").toString();

		String strReactionName = _prop.get("ReactionName").toString();
		String strReactionMetadataId = _prop.get("ReactionMetadataId")
				.toString();
		String strReactionReversible = _prop.get("ReactionReversible")
				.toString();

		// String szAnnotation = _prop.get("ReactionAnnotation").toString();
		String szNote = _prop.get("ReactionNotes").toString();

		String strLOWER_BOUND = _prop.get("LOWER_BOUND").toString();
		String[] _LBTokens = strLOWER_BOUND.split(" ");
		String strLB_Value = _LBTokens[0];
		String strLB_Unit = _LBTokens[1];

		String strUPPER_BOUND = _prop.get("UPPER_BOUND").toString();
		String[] _UBTokens = strUPPER_BOUND.split(" ");
		String strUB_Value = _UBTokens[0];
		String strUB_Unit = _UBTokens[1];

		String strFLUX_VALUE = _prop.get("FLUX_VALUE").toString();
		String[] _FVTokens = strFLUX_VALUE.split(" ");
		String strFV_Value = _FVTokens[0];
		String strFV_Unit = _FVTokens[1];

		String strOBJECTIVE_COEFFICIENT = _prop.get("OBJECTIVE_COEFFICIENT")
				.toString();
		String[] _OCTokens = strOBJECTIVE_COEFFICIENT.split(" ");
		String strOC_Value = _OCTokens[0];
		String strOC_Unit = _OCTokens[1];

		if (!foundReaction(strReactionId)) {
			Reaction eReaction = model.createReaction(strReactionId);
			eReaction.setName(strReactionName);
			eReaction.setMetaId(strReactionMetadataId);
			eReaction.setReversible(Boolean.valueOf(strReactionReversible));
			try {
				eReaction.setNotes(szNote);
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/*
			 * try { eReaction.setAnnotation(new Annotation(szAnnotation)); }
			 * catch (XMLStreamException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); }
			 */

			LocalParameter pLowerBound = new LocalParameter("LOWER_BOUND",
					model.getLevel(), model.getVersion());
			pLowerBound.setValue(Double.valueOf(strLB_Value));
			addUnitDefinition(strLB_Unit);
			pLowerBound.setUnits(strLB_Unit);

			LocalParameter pUpperBound = new LocalParameter("UPPER_BOUND",
					model.getLevel(), model.getVersion());
			pUpperBound.setValue(Double.valueOf(strUB_Value));
			addUnitDefinition(strUB_Unit);
			pUpperBound.setUnits(strUB_Unit);

			LocalParameter pFluxValue = new LocalParameter("FLUX_VALUE",
					model.getLevel(), model.getVersion());
			pFluxValue.setValue(Double.valueOf(strFV_Value));
			addUnitDefinition(strFV_Unit);
			pFluxValue.setUnits(strFV_Unit);

			LocalParameter pObjCoef = new LocalParameter(
					"OBJECTIVE_COEFFICIENT", model.getLevel(),
					model.getVersion());
			pObjCoef.setValue(Double.valueOf(strOC_Value));
			addUnitDefinition(strOC_Unit);
			pObjCoef.setUnits(strOC_Unit);

			KineticLaw kineticLaw = model.createKineticLaw();
			kineticLaw.addLocalParameter(pLowerBound);
			kineticLaw.addLocalParameter(pUpperBound);
			kineticLaw.addLocalParameter(pFluxValue);
			kineticLaw.addLocalParameter(pObjCoef);
			kineticLaw.setMath(new ASTNode(_prop.get("KineticLaw_Math")
					.toString()));
			eReaction.setKineticLaw(kineticLaw);
			eReaction.setSBOTerm(strSBOTermId);

			_ListOfReactions.put(_node.get("id").toString(), eReaction);
		}
	}

	private LabelTypes mapLabel(String _strLabel) {
		LabelTypes _label = LabelTypes.Protein;
		if (_strLabel.trim().toUpperCase().equals("PROTEIN")) {
			_label = LabelTypes.Protein;
		} else if (_strLabel.trim().toUpperCase().equals("REACTION")) {
			_label = LabelTypes.Reaction;
		} else if (_strLabel.trim().toUpperCase().equals("COMPLEX")) {
			_label = LabelTypes.Complex;
		} else if (_strLabel.trim().toUpperCase().equals("METABOLITE")) {
			_label = LabelTypes.Metabolite;
		} else if (_strLabel.trim().toUpperCase().equals("PROTEINCOMPOUND")) {
			_label = LabelTypes.ProteinCompound;
		}
		return _label;
	}

	private void addUnitDefinition(String strLB_Unit) {
		if (!strLB_Unit.equals("dimensionless")) {
			boolean bFound = false;

			for (String strUnitDef : _ListOfUnitDefinitions) {
				if (strUnitDef.equals(strLB_Unit.trim())) {
					bFound = true;
					break;
				}
			}

			if (!bFound) {
				UnitDefinition _unit = model.createUnitDefinition();
				_unit.setId(strLB_Unit);
				_ListOfUnitDefinitions.add(strLB_Unit);
			}
		}
	}

	private boolean foundReaction(String strReactionId) {
		boolean bFound = false;

		for (Entry<String, Reaction> strReact : _ListOfReactions.entrySet()) {
			if (strReact.getValue().getId().equals(strReactionId)) {
				bFound = true;
				break;
			}
		}
		return bFound;
	}

	private boolean foundCompartment(String strCompartmentName) {
		boolean bFound = false;

		for (String strCompName : _ListOfCompartments) {
			if (strCompName.equals(strCompartmentName)) {
				bFound = true;
				break;
			}
		}
		return bFound;
	}

	private boolean foundSpecies(String strSpeciesId) {
		boolean bFound = false;

		for (Entry<String, Species> strSpecies : _ListOfSpecies.entrySet()) {
			if (strSpecies.getValue().getId().equals(strSpeciesId)) {
				bFound = true;
				break;
			}
		}
		return bFound;
	}

	public void createOutputSIFFile(String szOutputSIFFilePath,
			String szNodeOutputSIFFilePath) {
		// TODO Auto-generated method stub
		PrintWriter outFileName = null;
		try {
			outFileName = new PrintWriter(new BufferedWriter(new FileWriter(
					szOutputSIFFilePath)));

			if (_ListOfReactionSpecies.size() != 0) {

				for (Entry<String, ReactionSpecies> eReaction : _ListOfReactionSpecies
						.entrySet()) {
					for (String strReactant : eReaction.getValue()._listOfReactants) {
						outFileName.println(strReactant + "\tConsumption\t"
								+ eReaction.getKey());
					}

					for (String strProduct : eReaction.getValue()._listOfProducts) {
						outFileName.println(eReaction.getKey()
								+ "\tProduction\t" + strProduct);
					}

					for (String strModifier : eReaction.getValue()._listOfModifiers) {
						outFileName.println(strModifier + "\tCatalysis\t"
								+ eReaction.getKey());
					}
				}
			}
		} catch (IOException e) {
			System.err.println(e);
		} finally {
			if (outFileName != null) {
				outFileName.close();
			}
		}

		PrintWriter outNodesFileName = null;
		try {
			outNodesFileName = new PrintWriter(new BufferedWriter(
					new FileWriter(szNodeOutputSIFFilePath)));
			if (_ListOfSpecies.size() != 0) {
				for (Entry<String, Species> eSpecies : _ListOfSpecies
						.entrySet()) {
					outNodesFileName.println(eSpecies.getValue().getName()
							+ "\t" + eSpecies.getValue().getSBOTermID() + "\t"
							+ eSpecies.getValue().getCompartment());
				}
			}

			if (_ListOfReactions.size() != 0) {
				for (Entry<String, Reaction> eReaction : _ListOfReactions
						.entrySet()) {
					outNodesFileName.println(eReaction.getValue().getName()
							+ "\t" + eReaction.getValue().getSBOTermID() + "\t"
							+ eReaction.getValue().getCompartment());
				}
			}
		} catch (IOException e) {
			System.err.println(e);
		} finally {
			if (outNodesFileName != null) {
				outNodesFileName.close();
			}
		}
	}
}
