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

import database.MyConnection;
import database.Users;
import server.main.Peer;
import utils.Utils;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

public class Login extends JFrame{

	public static Login frame;

	private JTextField email_input;
	private JPasswordField password_input;
	private JTextField bootstrap_input;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frame = new Login();
					frame.setVisible(true);
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
		this.setTitle("P2P Cloud Login");
		this.setResizable(false);
		this.setBounds(100, 100, 450, 300);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

		JLabel title_lbl = new JLabel("P2P Cloud");
		this.getContentPane().add(title_lbl, "2, 2, 3, 1, center, default");
		title_lbl.setHorizontalAlignment(SwingConstants.CENTER);
		title_lbl.setFont(new Font("Impact", Font.PLAIN, 19));

		JLabel email_lbl = new JLabel("Email");
		this.getContentPane().add(email_lbl, "2, 4, 3, 1, center, default");
		email_lbl.setHorizontalAlignment(SwingConstants.CENTER);

		email_input = new JTextField();
		this.getContentPane().add(email_input, "2, 6, 3, 1, center, default");
		email_input.setColumns(15);

		JLabel password_lbl = new JLabel("Password");
		this.getContentPane().add(password_lbl, "2, 8, 3, 1");
		password_lbl.setHorizontalAlignment(SwingConstants.CENTER);

		password_input = new JPasswordField();
		this.getContentPane().add(password_input, "2, 10, 3, 1, center, default");
		password_input.setColumns(15);

		JCheckBox save_info_check = new JCheckBox("Save login details");
		this.getContentPane().add(save_info_check, "2, 12, 3, 1, center, default");

		JCheckBox local_check = new JCheckBox("Local connection");
		getContentPane().add(local_check, "2, 18, center, default");

		bootstrap_input = new JTextField();
		bootstrap_input.setColumns(15);
		getContentPane().add(bootstrap_input, "4, 18, center, default");

		JButton login_btn = new JButton("Login");
		login_btn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Connection c;
				Login.frame.setEnabled(false);
				ProgressBar.frame = new ProgressBar();
				ProgressBar.frame.setVisible(true);
				ProgressBar.frame.setStatus("Connecting to database...");
				try {
					c = MyConnection.createConnection();
					ProgressBar.frame.setStatus("Attempt to login...");
					if(Users.isLoginCorrect(c, email_input.getText(), password_input.getText())){
						String email = email_input.getText();
						Random r = new Random();
						Peer.node = new Peer(email, r.nextInt(65535-1024)+1024);
						ProgressBar.frame.setStatus("Checking IP Address and Port...");
						Peer.node.initializeIPAddressesAndPorts(local_check.isSelected());
						ProgressBar.frame.setStatus("Joining P2P Cloud Network...");
						if(local_check.isSelected()){
							Peer.node.joinChordNetwork(!bootstrap_input.getText().equals("") ? bootstrap_input.getText():null);
						}else{
							Peer.node.joinChordNetwork("telmo20.ddns.net:8000");
						}
						Peer.node.initialize();
						Login.frame.setEnabled(true);
						ProgressBar.frame.dispose();
						FileManager.frame = new FileManager();
						FileManager.frame.setVisible(true);
						frame.dispose();
					}
					Login.frame.setEnabled(true);
					ProgressBar.frame.dispose();
				} catch (SQLException | ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					Login.frame.setEnabled(true);
					ProgressBar.frame.dispose();
					e1.printStackTrace();
				}
			}
		});
		this.getContentPane().add(login_btn, "2, 14, 3, 1, center, default");

		JButton register_btn = new JButton("Register new account");
		register_btn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Register.frame = new Register();
				Register.frame.setVisible(true);
				frame.setVisible(false);
			}
		});
		this.getContentPane().add(register_btn, "2, 16, 3, 1, center, default");


		register_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
	}

}
