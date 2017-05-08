package gui;


import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JPasswordField;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import database.MyConnection;
import database.Users;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.util.Arrays;

public class Register extends JFrame{

	public static Register frame;

	private JTextField email_input;
	private JPasswordField password_input;
	private JPasswordField password_confirm_input;

	/**
	 * Create the application.
	 */
	public Register() {
		super();
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Login.frame.setVisible(true);
			}
		});
		this.setTitle("P2P Client Register");
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

		JLabel title_lbl = new JLabel("P2P Client");
		this.getContentPane().add(title_lbl, "2, 2");
		title_lbl.setHorizontalAlignment(SwingConstants.CENTER);
		title_lbl.setFont(new Font("Impact", Font.PLAIN, 19));

		JLabel register_lbl = new JLabel("Register");
		register_lbl.setHorizontalAlignment(SwingConstants.CENTER);
		register_lbl.setFont(new Font("Tahoma", Font.BOLD, 12));
		this.getContentPane().add(register_lbl, "2, 4");

		JLabel email_lbl = new JLabel("Email");
		this.getContentPane().add(email_lbl, "2, 6");
		email_lbl.setHorizontalAlignment(SwingConstants.CENTER);

		email_input = new JTextField();
		this.getContentPane().add(email_input, "2, 8");
		email_input.setColumns(15);

		JLabel password_lbl = new JLabel("Password");
		this.getContentPane().add(password_lbl, "2, 10");
		password_lbl.setHorizontalAlignment(SwingConstants.CENTER);

		password_input = new JPasswordField();
		this.getContentPane().add(password_input, "2, 12");
		password_input.setColumns(15);

		JLabel password_confirm_lbl = new JLabel("Confirm your password");
		password_confirm_lbl.setHorizontalAlignment(SwingConstants.CENTER);
		this.getContentPane().add(password_confirm_lbl, "2, 14");

		password_confirm_input = new JPasswordField();
		password_confirm_input.setColumns(15);
		this.getContentPane().add(password_confirm_input, "2, 16");

		JButton register_btn = new JButton("Register");
		register_btn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if((password_input.getPassword().length >= 8) && password_input.getText().equals(password_confirm_input.getText())){
					Connection c = MyConnection.createConnection();
					if(Users.registerNewUser(c, email_input.getText(), password_input.getText())){
						frame.dispose();
						Login.frame.setVisible(true);
					}
				}
			}
		});
		register_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		this.getContentPane().add(register_btn, "2, 18");
	}

}
