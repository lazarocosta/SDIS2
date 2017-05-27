package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import database.Files;
import de.uniba.wiai.lspi.chord.console.command.entry.Key;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import server.main.Peer;
import server.protocol.Backup;
import server.protocol.Delete;

public class FileManager extends JFrame {

	public static FileManager frame;

	private JPanel contentPane;

	private JTree tree;
	private JPopupMenu fileOptionsMenu;
	private JLabel footer_lbl;

	private int[] fileIDs;

	/**
	 * Create the frame.
	 */
	public FileManager() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Peer.node.safeClose();
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

		JButton upload_btn = new JButton("Upload file");
		upload_btn.addMouseListener(new  MouseAdapter(){
			public void mouseClicked(MouseEvent arg0){
				JFileChooser uploadFileChooser = new JFileChooser();
				if (uploadFileChooser.showDialog(FileManager.this, "Upload") == JFileChooser.APPROVE_OPTION) {
					String message = "Please select file privacy you want?";
					JRadioButton privateButton = new JRadioButton("Private", true);
					JRadioButton publicButton = new JRadioButton("Public", false);
					ButtonGroup choices = new ButtonGroup();
					choices.add(privateButton);
					choices.add(publicButton);
					Object[] params = {message, privateButton, publicButton};
					if (JOptionPane.showConfirmDialog(null, params, "File privacy", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						//BACKUP START
						try {
							//Change to add size
							int fileID = Files.insertNewFile(Peer.connection, Peer.email, uploadFileChooser.getSelectedFile().getName(), publicButton.isSelected(),uploadFileChooser.getSelectedFile().length());
							System.out.println(fileID);

							Thread backupThread = new Thread(){
								public void run(){
									new Backup("1.0", "" +fileID, uploadFileChooser.getSelectedFile().getAbsolutePath(), 1);
								}
							};
							backupThread.start();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}
			}
		});
		contentPane.add(upload_btn, "2, 2, fill, default");

		JButton refresh_btn = new JButton("Refresh");
		refresh_btn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				refresh();
			}
		});
		contentPane.add(refresh_btn, "4, 2, fill, default");

		JButton btnNewButton_2 = new JButton("New button");
		contentPane.add(btnNewButton_2, "6, 2, fill, default");

		JButton public_files_btn = new JButton("Public files");
		public_files_btn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				PublicFiles.frame = new PublicFiles();
				PublicFiles.frame.setVisible(true);
				FileManager.frame.setEnabled(false);
			}
		});
		contentPane.add(public_files_btn, "8, 2, fill, default");

		fileOptionsMenu = new JPopupMenu();
		ActionListener actionListener = new PopupActionListener();

		JMenuItem restoreMenuItem = new JMenuItem("Restore");
		restoreMenuItem.addActionListener(actionListener);
		fileOptionsMenu.add(restoreMenuItem);

		JMenuItem deleteMenuItem = new JMenuItem("Delete");
		deleteMenuItem.addActionListener(actionListener);
		fileOptionsMenu.add(deleteMenuItem);

		tree = new JTree();
		tree.setRootVisible(false);
		tree.setModel(buildTreeModel());
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

				if (SwingUtilities.isRightMouseButton(e)) {

					int row = tree.getClosestRowForLocation(e.getX(), e.getY());
					tree.setSelectionRow(row);
					System.out.println(tree.getLeadSelectionRow());
					fileOptionsMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		contentPane.add(tree, "2, 4, 7, 1, fill, fill");

		JPanel footer = new JPanel();
		footer.setToolTipText("");
		footer.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		contentPane.add(footer, "1, 6, 9, 1, fill, fill");

		footer_lbl = new JLabel();
		updateFooter();
		footer.add(footer_lbl, "2, 2, left, top");

	}

	protected void refresh() {
		tree.setModel(buildTreeModel());
		updateFooter();
		Set<Serializable> paulo = Peer.chord.retrieve(new Key("AVAILABLE"));
		for(Serializable s : paulo){
			System.out.println(s.toString());
		}
	}

	private void updateFooter(){
		footer_lbl.setText(Peer.email + " , " + Peer.IPAddress + ":" + (Peer.port + 1) + " (" + Peer.chord.retrieve(new Key("AVAILABLE")).size() + " Peers)");
	}


	private DefaultTreeModel buildTreeModel(){
		DefaultTreeModel dtm;
		dtm = new DefaultTreeModel(
				new DefaultMutableTreeNode("Files") {
					{
						try{
							ArrayList<String[]> files = Files.getFileNames(Peer.connection, Peer.email);
							fileIDs = new int[files.size()];
							for(int i=0; i < files.size(); i++){
								add(new DefaultMutableTreeNode(String.format("%-40s (%s)",files.get(i)[1],files.get(i)[2])));
								fileIDs[i] = Integer.parseInt(files.get(i)[0]); 
							}

						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				}
				);
		return dtm;
	}

	class PopupActionListener implements ActionListener {
		public void actionPerformed(ActionEvent actionEvent) {
			if(actionEvent.getActionCommand().equals("Delete")){
				//DELETE
				int fileID = fileIDs[tree.getLeadSelectionRow()];
				try {
					Files.moveFileToDeleted(Peer.connection, fileID);

					new Thread(new Delete(Peer.protocolVersion, ""+fileID)).start();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				refresh();


			}else if(actionEvent.getActionCommand().equals("Restore")){
				//RESTORE
				int fileID = fileIDs[tree.getLeadSelectionRow()];
				JFileChooser selectFolder = new JFileChooser();
				selectFolder.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (selectFolder.showOpenDialog(FileManager.this) == JFileChooser.APPROVE_OPTION) { 
					System.out.println("getCurrentDirectory(): " + selectFolder.getCurrentDirectory());
					//RESTORE START
						Thread restoreThread = new Thread(){
							public void run(){
								//new Restore("1.0", "" +fileID, selectFolder.getCurrentDirectory());
							}
						};
						restoreThread.start();
				}else {
					System.out.println("No Selection ");
				}

		}
		System.out.println("Selected: " + actionEvent.getActionCommand());
	}
}

}
