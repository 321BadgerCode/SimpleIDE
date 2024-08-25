import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.regex.*;
import java.util.Timer;
import java.util.TimerTask;

public class SimpleIDE extends JFrame {
	private JTextPane textPane;
	private StyledDocument doc;
	private Style defaultStyle, keywordStyle, datatypeStyle, operatorStyle, functionStyle, commentStyle, stringStyle, literalStyle, constantStyle;
	private JFileChooser fileChooser;

	private final Pattern keywordPattern = Pattern.compile("\\b(public|private|protected|class|static|return|if|else|for|while|switch|case|break|continue)\\b");
	private final Pattern datatypePattern = Pattern.compile("\\b(byte|short|int|long|float|double|char|boolean|String|void)\\b");
	private final Pattern operatorPattern = Pattern.compile("[+\\-*/%=&|^!<>]");
	private final Pattern functionPattern = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*(?=\\()");
	private final Pattern commentPattern = Pattern.compile("//.*");
	private final Pattern stringPattern = Pattern.compile("\"(\\\\\"|\\\\n|[^\"])*\"");
	private final Pattern literalPattern = Pattern.compile("\\b(true|false|null)\\b");
	private final Pattern constantPattern = Pattern.compile("\\b(\\d+\\.\\d*|\\.\\d+|\\d+)([eE][+\\-]?\\d+)?\\b|\\b0[xX][0-9A-Fa-f]+\\b");

	private Timer syntaxHighlightingTimer;

	public SimpleIDE() {
		setTitle("Simple IDE");
		setSize(800, 600);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		textPane = new JTextPane();
		doc = textPane.getStyledDocument();
		fileChooser = new JFileChooser();
		syntaxHighlightingTimer = new Timer();

		textPane.setBackground(new Color(18, 18, 18)); // Very dark gray
		textPane.setForeground(Color.WHITE); // Light color for text
		textPane.setCaretColor(Color.WHITE); // Light color for caret
		textPane.setSelectionColor(new Color(0, 150, 255)); // Bright blue for selection background
		textPane.setSelectedTextColor(Color.WHITE); // Light color for selected text
		textPane.setMargin(new Insets(5, 5, 5, 5));
		textPane.setFont(new Font("Monospaced", Font.PLAIN, 14));

		setupStyles();
		setupMenuBar();
		setupTextPane();

		add(new JScrollPane(textPane), BorderLayout.CENTER);
		setVisible(true);
	}

	private void setupStyles() {
		defaultStyle = doc.addStyle("default", null);
		StyleConstants.setFontFamily(defaultStyle, "Monospaced");
		StyleConstants.setFontSize(defaultStyle, 14);
		StyleConstants.setForeground(defaultStyle, Color.WHITE);

		keywordStyle = doc.addStyle("keyword", null);
		StyleConstants.setForeground(keywordStyle, new Color(128, 0, 255)); // Purple for keywords

		datatypeStyle = doc.addStyle("datatype", null);
		StyleConstants.setForeground(datatypeStyle, new Color(0, 150, 255)); // Bright blue for data types

		operatorStyle = doc.addStyle("operator", null);
		StyleConstants.setForeground(operatorStyle, new Color(255, 193, 7)); // Bright yellow for operators

		functionStyle = doc.addStyle("function", null);
		StyleConstants.setForeground(functionStyle, new Color(255, 255, 0)); // Light yellow for functions

		commentStyle = doc.addStyle("comment", null);
		StyleConstants.setForeground(commentStyle, new Color(128, 255, 0)); // Green for comments

		stringStyle = doc.addStyle("string", null);
		StyleConstants.setForeground(stringStyle, new Color(255, 87, 34)); // Orange for strings

		literalStyle = doc.addStyle("literal", null);
		StyleConstants.setForeground(literalStyle, new Color(0, 191, 255)); // Light cyan for literals

		constantStyle = doc.addStyle("constant", null);
		StyleConstants.setForeground(constantStyle, new Color(255, 87, 34)); // Orange for constants
	}

	private void setupTextPane() {
		textPane.setFont(new Font("Monospaced", Font.PLAIN, 14));
		textPane.setCaretPosition(0);

		textPane.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				scheduleSyntaxHighlighting();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				scheduleSyntaxHighlighting();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				scheduleSyntaxHighlighting();
			}
		});
	}

	private void scheduleSyntaxHighlighting() {
		if (syntaxHighlightingTimer != null) {
			syntaxHighlightingTimer.cancel();
		}
		syntaxHighlightingTimer = new Timer();
		syntaxHighlightingTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				SwingUtilities.invokeLater(SimpleIDE.this::applySyntaxHighlighting);
			}
		}, 100); // Delay in milliseconds
	}

	private void applySyntaxHighlighting() {
		try {
			doc.setCharacterAttributes(0, doc.getLength(), defaultStyle, true);

			String text = textPane.getText();
			applyHighlights(text, keywordPattern, keywordStyle);
			applyHighlights(text, datatypePattern, datatypeStyle);
			applyHighlights(text, operatorPattern, operatorStyle);
			applyHighlights(text, functionPattern, functionStyle);
			applyHighlights(text, commentPattern, commentStyle);
			applyHighlights(text, stringPattern, stringStyle);
			applyHighlights(text, literalPattern, literalStyle);
			applyHighlights(text, constantPattern, constantStyle);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void applyHighlights(String text, Pattern pattern, Style style) {
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			doc.setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), style, false);
		}
	}

	private void setupMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
		JMenuItem openItem = new JMenuItem("Open");
		openItem.addActionListener(e -> openFile());
		JMenuItem saveItem = new JMenuItem("Save");
		saveItem.addActionListener(e -> saveFile());
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		menuBar.add(fileMenu);

		JMenu editMenu = new JMenu("Edit");
		JMenuItem findItem = new JMenuItem("Find");
		findItem.addActionListener(e -> findText());
		JMenuItem replaceItem = new JMenuItem("Replace");
		replaceItem.addActionListener(e -> replaceText());
		editMenu.add(findItem);
		editMenu.add(replaceItem);
		menuBar.add(editMenu);

		JMenu infoMenu = new JMenu("Info");
		JMenuItem aboutItem = new JMenuItem("About");
		aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this, "Simple IDE v1.0.0\n\nA simple text editor with syntax highlighting."));
		infoMenu.add(aboutItem);
		JMenuItem authorItem = new JMenuItem("Author");
		authorItem.addActionListener(e -> JOptionPane.showMessageDialog(this, "Author: Badger Code"));
		infoMenu.add(authorItem);
		menuBar.add(infoMenu);

		setJMenuBar(menuBar);
	}

	private void openFile() {
		JFileChooser fileChooser = new JFileChooser();
		int option = fileChooser.showOpenDialog(this);
		if (option == JFileChooser.APPROVE_OPTION) {
			try {
				File file = fileChooser.getSelectedFile();
				FileReader reader = new FileReader(file);
				textPane.read(reader, null);
				reader.close();

				textPane.setCaretPosition(0);
				setTitle(file.getName());

				doc = textPane.getStyledDocument();
				doc.addDocumentListener(new DocumentListener() {
					@Override
					public void insertUpdate(DocumentEvent e) {
						scheduleSyntaxHighlighting();
					}

					@Override
					public void removeUpdate(DocumentEvent e) {
						scheduleSyntaxHighlighting();
					}

					@Override
					public void changedUpdate(DocumentEvent e) {
						scheduleSyntaxHighlighting();
					}
				});

				applySyntaxHighlighting();
			} catch (Exception ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(this, "Error opening file: " + ex.getMessage());
			}
		}
	}

	private void saveFile() {
		int returnValue = fileChooser.showSaveDialog(this);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
				textPane.write(writer);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private void findText() {
		String search = JOptionPane.showInputDialog("Find:");
		if (search != null && !search.isEmpty()) {
			try {
				Highlighter highlighter = textPane.getHighlighter();
				highlighter.removeAllHighlights();
				String text = textPane.getText();
				Pattern pattern = Pattern.compile(Pattern.quote(search));
				Matcher matcher = pattern.matcher(text);
				while (matcher.find()) {
					highlighter.addHighlight(matcher.start(), matcher.end(), new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 255, 0, 128))); // Semi-transparent yellow
				}
			} catch (BadLocationException ex) {
				ex.printStackTrace();
			}
		}
	}

	private void replaceText() {
		String find = JOptionPane.showInputDialog("Find:");
		String replace = JOptionPane.showInputDialog("Replace with:");
		if (find != null && replace != null) {
			String text = textPane.getText();
			text = text.replace(find, replace);
			textPane.setText(text);
			applySyntaxHighlighting();
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(SimpleIDE::new);
	}
}