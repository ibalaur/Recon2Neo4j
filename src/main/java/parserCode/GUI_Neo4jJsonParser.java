package parserCode;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.json.simple.parser.ParseException;

import java.io.IOException;

public class GUI_Neo4jJsonParser {
	protected static String szInputFilePath = null;
	protected static String szOutputFilePath = null;
	protected static String szLogsFilePath = null;
	protected static String szOutputSIFFilePath = null;
	protected static String szOutputNodesSIFFilePath = null;

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			final JFrame frame = new JFrame("Neo4j to SBML Parser");

			JPanel panel = new JPanel();

			panel.setLayout(null);

			final JTextField jtfOpenFilePath = new JTextField(70);
			final JTextField jtfSaveFilePath = new JTextField(70);
			final JTextField jtfSIFFilePath = new JTextField(70);

			jtfOpenFilePath.setBounds(265, 5, 600, 28);
			jtfSaveFilePath.setBounds(265, 55, 600, 28);
			jtfSIFFilePath.setBounds(265, 155, 600, 28);

			final JButton btnOpenJsonInput = new JButton();
			btnOpenJsonInput.setBounds(10, 5, 218, 28);
			btnOpenJsonInput.setText("Open JSON Input File");
			btnOpenJsonInput.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle("Open a JSON file to parse");
					fileChooser
							.addChoosableFileFilter(new FileNameExtensionFilter(
									"JSON Files", "json"));
					int userSelection = fileChooser.showOpenDialog(null);
					if (userSelection == JFileChooser.APPROVE_OPTION) {
						szInputFilePath = fileChooser.getSelectedFile()
								.getAbsolutePath();
						jtfOpenFilePath.setText(szInputFilePath);
					}
				}
			});

			final JButton btnSaveSmblOutput = new JButton();
			btnSaveSmblOutput.setBounds(10, 55, 218, 28);
			btnSaveSmblOutput.setText("Save SMBL output file");
			btnSaveSmblOutput.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle("Specify a file to save");

					int userSelection = fileChooser.showSaveDialog(null);

					if (userSelection == JFileChooser.APPROVE_OPTION) {
						szOutputFilePath = fileChooser.getSelectedFile()
								.getAbsolutePath();

						int iPathSize = szOutputFilePath.length();
						String strLatestInfo = szOutputFilePath.substring(
								iPathSize - 4, iPathSize);
						if (!strLatestInfo.toLowerCase().equals(".xml")) {
							szOutputFilePath = szOutputFilePath.concat(".xml");
						}

						jtfSaveFilePath.setText(szOutputFilePath);
						
						szLogsFilePath = fileChooser.getSelectedFile().getParent().concat("/logs.txt");
					}
				}
			});

			final JButton btnSIFOutputFile = new JButton();
			btnSIFOutputFile.setBounds(10, 155, 218, 28);
			btnSIFOutputFile.setText("Save SIF output file");
			btnSIFOutputFile.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser
							.setDialogTitle("Specify a file to save SIF format");

					int userSelection = fileChooser.showSaveDialog(null);

					if (userSelection == JFileChooser.APPROVE_OPTION) {
						szOutputSIFFilePath = fileChooser.getSelectedFile()
								.getAbsolutePath();
						int iPathSize = szOutputSIFFilePath.length();
						String strExtension = szOutputSIFFilePath.substring(
								iPathSize - 4, iPathSize);
						if (!strExtension.toLowerCase().equals(".sif")) {
							szOutputSIFFilePath = szOutputSIFFilePath
									.concat(".sif");
						}

						jtfSIFFilePath.setText(szOutputSIFFilePath);
						
						szOutputNodesSIFFilePath = fileChooser.getSelectedFile().getParent().concat("/nodes.sif");
						
					}
				}
			});

			JButton btnParse = new JButton();
			btnParse.setBounds(10, 205, 218, 28);
			btnParse.setText("Parse JSON to SBML");
			btnParse.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JSON2SBMLParser _parser = new JSON2SBMLParser();

					if (null == szInputFilePath) {
						JOptionPane.showMessageDialog(frame,
								"Error: No path for the input JSON file! Press "
										+ btnOpenJsonInput.getText()
										+ " button to load JSON file!",
								"No input JSON file!",
								JOptionPane.ERROR_MESSAGE);
					}

					else if (null == szOutputFilePath) {
						JOptionPane.showMessageDialog(frame,
								"Error: No path for the output SBML file! Press "
										+ btnSaveSmblOutput.getText()
										+ " button to save SBML file!",
								"No output SBML file path!",
								JOptionPane.ERROR_MESSAGE);
					}

					else {
							try {
								_parser.parseJSONString(szInputFilePath);
							} catch (IOException | ParseException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
							_parser.createOutputSBMLFile(szOutputFilePath,
									szLogsFilePath);

							if(null != szLogsFilePath )
							{
							JOptionPane
								.showMessageDialog(
										frame,
										"Parsing finished: SBML file available at: "
												+ szOutputFilePath
												+ "\nSBML model consistency checked: Logs available at: "
												+ szLogsFilePath);
							}
					}
				}
			});

			JButton btnParseSIF = new JButton();
			btnParseSIF.setBounds(265, 205, 218, 28);
			btnParseSIF.setText("Parse JSON to SIF");
			btnParseSIF.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JSON2SBMLParser _parser = new JSON2SBMLParser();
					if (null == szInputFilePath) {
						JOptionPane.showMessageDialog(frame,
								"Error: No path for the input JSON file! Press "
										+ btnOpenJsonInput.getText()
										+ " button to load JSON file!",
								"No input JSON file!",
								JOptionPane.ERROR_MESSAGE);
					}

					else if (null == szOutputSIFFilePath) {
						JOptionPane.showMessageDialog(frame,
								"Error: No path for the output SIF file! Press "
										+ btnSIFOutputFile.getText()
										+ " button to save SBML file!",
								"No output SBML file path!",
								JOptionPane.ERROR_MESSAGE);
					}
					else
					{
						try {
							_parser.parseJSONString(szInputFilePath);
						} catch (IOException | ParseException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
					_parser.createOutputSIFFile(szOutputSIFFilePath, szOutputNodesSIFFilePath);

					JOptionPane.showMessageDialog(frame,
							"Parsing finished: SIF file available at: "
									+ szOutputSIFFilePath);
					}
				}
			});

			JButton btnClose = new JButton();
			btnClose.setBounds(520, 205, 147, 28);
			btnClose.setText("Close");
			btnClose.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frame.dispose();
				}
			});

			JLabel _fundingLabel = new JLabel("@Copyright: EISBM - eTRIKS");
			_fundingLabel.setBounds(700, 205, 300, 28);

			panel.add(btnOpenJsonInput);
			panel.add(btnSaveSmblOutput);
			panel.add(btnSIFOutputFile);
			panel.add(btnParse);
			panel.add(btnParseSIF);
			panel.add(btnClose);

			panel.add(jtfOpenFilePath);
			panel.add(jtfSaveFilePath);
			panel.add(jtfSIFFilePath);
			panel.add(_fundingLabel);

			frame.add(panel);
			frame.setSize(900, 270);
			frame.setLocationRelativeTo(null);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
