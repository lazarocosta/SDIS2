package gui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import database.MyConnection;
import database.Users;

@SuppressWarnings("serial")
public class Register extends JFrame {

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
		this.setTitle("P2P Cloud Register");
		this.setResizable(false);
		this.setBounds(100, 100, 450, 300);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		FormLayout formLayout = new FormLayout(
				new ColumnSpec[] { FormSpecs.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("center:default:grow"),
						FormSpecs.LABEL_COMPONENT_GAP_COLSPEC, },
				new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
						FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormSpecs.RELATED_GAP_ROWSPEC,
						RowSpec.decode("default:grow"), FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
						FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormSpecs.RELATED_GAP_ROWSPEC,
						RowSpec.decode("default:grow"), FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
						FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormSpecs.RELATED_GAP_ROWSPEC,
						RowSpec.decode("default:grow"), FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
						FormSpecs.RELATED_GAP_ROWSPEC, });
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

		JLabel message_lbl = new JLabel();
		message_lbl.setVisible(false);
		this.getContentPane().add(message_lbl, "2, 18");
		message_lbl.setHorizontalAlignment(SwingConstants.CENTER);

		JButton register_btn = new JButton("Register");
		register_btn.addMouseListener(new MouseAdapter() {
			@SuppressWarnings("deprecation")
			@Override
			public void mouseClicked(MouseEvent e) {
				if ((password_input.getPassword().length >= 8)
						&& password_input.getText().equals(password_confirm_input.getText())) {
					Connection c;
					try {
						c = MyConnection.createConnection();

						if (Users.registerNewUser(c, email_input.getText(), password_input.getText())) {
							frame.dispose();
							Login.frame.setVisible(true);
						}
					} catch (ClassNotFoundException | SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} else {
					if (password_input.getPassword().length < 8)
						message_lbl.setText("Password has to be at least 8 characters long.");

					if (!(password_input.getText().equals(password_confirm_input.getText())))
						message_lbl.setText("Passwords do not match.");

					message_lbl.setVisible(true);
				}
			}
		});
		register_btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {

			}
		});
		this.getContentPane().add(register_btn, "2, 20");
	}

}
