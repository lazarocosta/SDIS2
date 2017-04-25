package gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPasswordField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Login extends JFrame{
	
	public static Login mainWindow;

	private JTextField email_input;
	private JPasswordField password_input;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					mainWindow = new Login();
					mainWindow.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Login() {
		super();
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		this.setTitle("P2P Client Login");
		this.setResizable(false);
		this.setBounds(100, 100, 450, 300);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
		
		JLabel email_lbl = new JLabel("Email");
		this.getContentPane().add(email_lbl, "2, 4");
		email_lbl.setHorizontalAlignment(SwingConstants.CENTER);
		
		email_input = new JTextField();
		this.getContentPane().add(email_input, "2, 6");
		email_input.setColumns(15);
		
		JLabel password_lbl = new JLabel("Password");
		this.getContentPane().add(password_lbl, "2, 8");
		password_lbl.setHorizontalAlignment(SwingConstants.CENTER);
		
		password_input = new JPasswordField();
		this.getContentPane().add(password_input, "2, 10");
		password_input.setColumns(15);
		
		JCheckBox save_info_check = new JCheckBox("Save login details");
		this.getContentPane().add(save_info_check, "2, 12");
		
		JButton login_btn = new JButton("Login");
		login_btn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				FileManager fileManager = new FileManager();
				fileManager.setVisible(true);
				mainWindow.dispose();
			}
		});
		this.getContentPane().add(login_btn, "2, 14");
		
		JButton register_btn = new JButton("Register new account");
		register_btn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Register registerWindow = new Register();
				registerWindow.setVisible(true);
				mainWindow.setVisible(false);
			}
		});
		this.getContentPane().add(register_btn, "2, 16");
		register_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		
		JButton forgot_password_btn = new JButton("Forgot your password?");
		this.getContentPane().add(forgot_password_btn, "2, 18");
	}

}
