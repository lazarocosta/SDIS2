package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import database.Files;
import de.uniba.wiai.lspi.chord.console.command.entry.Key;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import server.main.Peer;
import server.task.initiatorPeer.Delete;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JScrollPane;

public class PublicFiles extends JFrame {

	public static PublicFiles frame;

	private JPanel contentPane;

	private JTree tree;
	private JPopupMenu fileOptionsMenu;

	private int[] fileIDs;

	/**
	 * Create the frame.
	 */
	public PublicFiles() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				FileManager.frame.setEnabled(true);
			}
		});
		setTitle("P2P Cloud Public Files");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("default:grow"),},
				new RowSpec[] {
						RowSpec.decode("default:grow"),}));

		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane, "1, 1, fill, fill");

		tree = new JTree();
		tree.setModel(buildTreeModel());
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

				if (SwingUtilities.isLeftMouseButton(e)) {
					if(e.getClickCount() == 2){
						int row = tree.getClosestRowForLocation(e.getX(), e.getY());
						tree.setSelectionRow(row);
						System.out.println(tree.getLeadSelectionRow());
						if(tree.getLeadSelectionRow() > 0){
							JFileChooser selectFolder = new JFileChooser();
							selectFolder.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
							if (selectFolder.showOpenDialog(PublicFiles.this) == JFileChooser.APPROVE_OPTION) { 
								System.out.println("getCurrentDirectory(): " + selectFolder.getCurrentDirectory());
							}else {
								System.out.println("No Selection ");
							}
						}
					}
				}
			}
		});
		scrollPane.setViewportView(tree);


	}

	private DefaultTreeModel buildTreeModel(){
		DefaultTreeModel dtm = new DefaultTreeModel(
				new DefaultMutableTreeNode("Public files") {
					{
						try{
							ArrayList<String[]> files = Files.getPublicFiles(Peer.connection);
							fileIDs = new int[files.size()];
							for(int i=0; i < files.size(); i++){
								add(new DefaultMutableTreeNode(String.format("%-40s (%s)",files.get(i)[1],files.get(i)[2])));
								fileIDs[i] = Integer.parseInt(files.get(i)[0]); 
							}

						} catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
				);
		return dtm;
	}

}
