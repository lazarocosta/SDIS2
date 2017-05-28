package gui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import javax.crypto.spec.SecretKeySpec;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import database.UsersKeys;
import security.HybridEncryption;
import server.Peer;

@SuppressWarnings("serial")
public class Login extends JFrame {

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
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(dim.width / 3, dim.height / 2);
		this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		FormLayout formLayout = new FormLayout(
				new ColumnSpec[] { FormSpecs.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("center:default:grow"),
						FormSpecs.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("center:default:grow"),
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
		getContentPane().add(local_check, "2, 20, center, default");

		bootstrap_input = new JTextField();
		bootstrap_input.setColumns(15);
		getContentPane().add(bootstrap_input, "4, 20, center, default");

		JLabel message_lbl = new JLabel();
		message_lbl.setVisible(false);
		this.getContentPane().add(message_lbl, "2, 18, 3, 1, center, default");
		message_lbl.setHorizontalAlignment(SwingConstants.CENTER);

		JButton login_btn = new JButton("Login");
		login_btn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// LOGIN PROCEDURE
				ProgressBar.frame = new ProgressBar();
				ProgressBar.frame.setVisible(true);
				ProgressBar.frame.setStatus("Connecting to database...");
				Login.frame.setEnabled(false);
				Thread t = new Thread() {
					public void run() {
						try {
							Peer.connection = MyConnection.createConnection();
							ProgressBar.frame.setStatus("Attempt to login...");
							@SuppressWarnings("deprecation")
							int id = Users.isLoginCorrect(Peer.connection, email_input.getText(),
									password_input.getText());
							if (id != -1) {
								Peer.id = id;
								String email = email_input.getText();
								Random r = new Random();
								Peer.email = email;
								Peer.port = r.nextInt(65535 - 1024) + 1024;

                                Peer.hybridEncryption = new HybridEncryption();
                                System.out.println("Peer_userid" +Peer.id);

								ResultSet userKey = UsersKeys.loadUserKey(Peer.connection, Peer.id);
								if (userKey != null) {


                                    KeyFactory kf = null; // or "EC" or whatever
                                    try {
                                        kf = KeyFactory.getInstance("RSA");

									} catch (NoSuchAlgorithmException e ) {
										e.printStackTrace();
									}

									byte[] privateKeyBytes= userKey.getBytes("assymmetrickeyprivate");
                                    byte[] publicKeyBytes = userKey.getBytes("assymmetrickeypublic");
                                    String secretKeySpec = userKey.getString("symmetrickey");

									Peer.hybridEncryption.bytesToAsymmetricKeyPrivate(kf, privateKeyBytes);
									Peer.hybridEncryption.bytesToAsymmetricKeyPublic(kf, publicKeyBytes);
									Peer.hybridEncryption.stringToSymmetricKey(secretKeySpec);


                                } else {
                                    Peer.hybridEncryption.generateKeys();

                                    PrivateKey privateKey= Peer.hybridEncryption.getAsymmetricPrivateKey();
                                    PublicKey publicKey = Peer.hybridEncryption.getAsymmetricPublicKey();
									String secretKeySpec = Peer.hybridEncryption.symmetricKeyToString();

                                    UsersKeys.insertUserKey(Peer.connection,Peer.id,privateKey.getEncoded(), publicKey.getEncoded(),secretKeySpec );
                                }


                                ProgressBar.frame.setStatus("Checking IP Address and Port...");
								if (Peer.initializeIPAddressesAndPorts(local_check.isSelected())) {
									ProgressBar.frame.setStatus("Initializing UDP listening...");
									Peer.startListening();

									if (!Peer.port_forwarded) {
										ProgressBar.frame.setStatus("Holepunching NAT...");
										// Peer.udpHolePunch();
									}

									boolean joined = false;
									ProgressBar.frame.setStatus("Joining P2P Cloud Network...");
									if (local_check.isSelected()) {
										joined = Peer.joinChordNetwork(!bootstrap_input.getText().equals("")
												? bootstrap_input.getText() : null);
									} else {
										joined = Peer.joinChordNetwork("telmo20.ddns.net:8080");
									}
									if (joined) {
										ProgressBar.frame.setStatus("Initializing file system...");
										Peer.initialize();

										ProgressBar.frame.setStatus("Updating data folder...");
										Peer.updateFileSystem();

										ProgressBar.frame.setStatus("Announcing my chunks...");
										Peer.insertMyFiles();

										ProgressBar.frame.setStatus("Login successful!");

										closeProgressBarAndResumeLogin();
										Login.frame.setEnabled(false);
										FileManager.frame = new FileManager();
										FileManager.frame.setVisible(true);
										frame.dispose();
									}
								}
							} else {
								Peer.connection.close();
								message_lbl.setText("Incorrect username and/or password.");
								message_lbl.setVisible(true);
							}
							closeProgressBarAndResumeLogin();
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							closeProgressBarAndResumeLogin();
							e1.printStackTrace();
						}
					}
				};
				t.start();
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

	private void closeProgressBarAndResumeLogin() {
		Login.frame.setEnabled(true);
		ProgressBar.frame.dispose();
	}

}
