package gui;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import javax.swing.JProgressBar;

public class ProgressBar extends JFrame {

	public static ProgressBar frame;
	
	private JLabel status_lbl;

	/**
	 * Create the frame.
	 */
	public ProgressBar() {
		super();
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		this.setTitle("P2P Cloud");
		this.setResizable(false);
		this.setBounds(100, 100, 450, 300);
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
		this.getContentPane().add(progressBar, "2, 4");
		
		JButton cancel_btn = new JButton("Cancel");
		this.getContentPane().add(cancel_btn, "2, 6");
		
	}
	
	public void setStatus(String status){
		this.status_lbl.setText(status);
	}

}
