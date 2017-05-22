package gui;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import javax.swing.JProgressBar;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ProgressBar extends JDialog {

	public static ProgressBar frame;

	private JLabel status_lbl;

	/**
	 * Create the frame.
	 */
	public ProgressBar() {
		super();
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				Login.frame.setEnabled(true);
			}
		});
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		this.setTitle("P2P Cloud");
		this.setResizable(false);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(dim.width/3,dim.height/3);
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		FormLayout formLayout = new FormLayout(new ColumnSpec[] {
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
						FormSpecs.RELATED_GAP_ROWSPEC,});
		this.getContentPane().setLayout(formLayout);

		status_lbl = new JLabel("Status");
		this.getContentPane().add(status_lbl, "2, 2");

		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		this.getContentPane().add(progressBar, "2, 4");

		JButton cancel_btn = new JButton("Cancel");
		cancel_btn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Login.frame.setEnabled(true);
				ProgressBar.frame.dispose();
			}
		});
		this.getContentPane().add(cancel_btn, "2, 6");

	}

	public void setStatus(String status){
		this.status_lbl.setText(status);
	}

}
