package gui;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;

import org.xml.sax.SAXException;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import server.main.Peer;

import javax.swing.border.BevelBorder;
import javax.swing.JLabel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class FileManager extends JFrame {

	public static FileManager frame;

	private JPanel contentPane;

	/**
	 * Create the frame.
	 */
	public FileManager() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Peer.safeClose();
			}
		});
		setTitle("P2P Cloud File Manager");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu file_menu = new JMenu("File");
		menuBar.add(file_menu);
		
		JMenuItem upload_item = new JMenuItem("Upload new file");
		file_menu.add(upload_item);
		
		JMenu popup_teste = new JMenu("Popup Teste");
		JMenuItem popup_1 = new JMenuItem("Teste 1");
		popup_teste.add(popup_1);

		file_menu.add(popup_teste);
		
		JMenuItem exit_item = new JMenuItem("Exit");
		file_menu.add(exit_item);
		
		JMenu options_menu = new JMenu("Options");
		menuBar.add(options_menu);
		
		JMenuItem preferences_item = new JMenuItem("Preferences");
		preferences_item.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				Preferences.frame = new Preferences();
				Preferences.frame.setVisible(true);
				//options_menu.setPopupMenuVisible(false);
				frame.setEnabled(false);
			}
		});
		
		JMenuItem change_password_item = new JMenuItem("Change password");
		options_menu.add(change_password_item);
		options_menu.add(preferences_item);
		
		JMenu help_menu = new JMenu("Help");
		menuBar.add(help_menu);
		
		JMenuItem about_us_item = new JMenuItem("About us...");
		help_menu.add(about_us_item);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,}));
		
		JButton btnNewButton = new JButton("New button");
		contentPane.add(btnNewButton, "2, 2");
		
		JButton btnNewButton_1 = new JButton("New button");
		contentPane.add(btnNewButton_1, "4, 2");
		
		JButton btnNewButton_2 = new JButton("New button");
		contentPane.add(btnNewButton_2, "6, 2");
		
		JButton btnNewButton_3 = new JButton("New button");
		contentPane.add(btnNewButton_3, "8, 2");
		
		JTree tree = new JTree();
		contentPane.add(tree, "2, 4, 7, 1, fill, fill");
		
		JPanel footer = new JPanel();
		footer.setToolTipText("");
		footer.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		contentPane.add(footer, "1, 6, 9, 1, fill, fill");
		try {
			JLabel footer_lbl = new JLabel(Peer.email + " , " + Peer.activeGW.getExternalIPAddress() + ":" + Peer.port + " (CONNECTED)");
			footer.add(footer_lbl, "2, 2, left, top");
		} catch (IOException | SAXException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	private static void addPopup(Component component, final JPopupMenu popup) {
	}
}
