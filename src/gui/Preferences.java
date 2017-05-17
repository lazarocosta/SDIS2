package gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import server.main.Peer;

public class Preferences extends JFrame{
	
	public static Preferences frame;
	/**
	 * Create the application.
	 */
	public Preferences() {
		super();
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				FileManager.frame.setEnabled(true);
			}
		});
		this.setTitle("Preferences");
		this.setBounds(100, 100, 170, 90);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		this.setType(Type.UTILITY);
		FormLayout formLayout = new FormLayout(new ColumnSpec[] {
				FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("center:default:grow"),
				FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("center:default:grow"),
				FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_ROWSPEC,});
		this.getContentPane().setLayout(formLayout);
		
		JLabel port_lbl = new JLabel("Port");
		getContentPane().add(port_lbl, "2, 2, fill, default");
		
		JSpinner port_input = new JSpinner();
		port_input.setModel(new SpinnerNumberModel(1024, 1024, 65535, 1));
		getContentPane().add(port_input, "4, 2, fill, default");
		
		JButton confirm_btn = new JButton("Confirm");
		confirm_btn.setVerticalAlignment(SwingConstants.BOTTOM);
		confirm_btn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Peer.node.setPort((int) port_input.getValue());
				FileManager.frame.setEnabled(true);
				Preferences.frame.dispose();
			}
		});
		getContentPane().add(confirm_btn, "2, 4, fill, default");
		
		JButton cancel_btn = new JButton("Cancel");
		cancel_btn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				FileManager.frame.setEnabled(true);
				Preferences.frame.dispose();
			}
		});
		getContentPane().add(cancel_btn, "4, 4, fill, default");
	}

}
