package org.shersfy.oss.boot;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;

public class Application extends JFrame implements ActionListener{
	
	Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Font FONT_SIZE = new Font(null, Font.CENTER_BASELINE, 16);
	
	private static final int MARGIN		 = 10;
	private static final int MARGIN_LEFT = 20;
	private static final int MARGIN_TOP  = 20;
	private static final int TXT_LEN  	 = 18;
	
	private static final String endPoint 		= "http://oss-cn-beijing.aliyuncs.com";
	private static final String accessKeyId		= "LTAIYNlyYMVaDSOt";
	private static final String secretAccessKey	= "";
	private static final String bucket			= "";

	private static final String CONF_PATH = "log4j.properties";
	
	// UI
	private JPanel mainPane;
	private JTextField txtEndPoint;
	private JTextField txtAccessKeyId;
	private JTextField txtSecretAccessKey;
	private JTextField txtBucket;
	private JTextField txtProject;
	private JTextField txtInputPath;
	
	// config
	private Properties config;
	
	
	public Application(String title){
		init();
		initUI(title);
	}

	public static void main(String[] args) {
		new Application("阿里云OSS客户端");
	}
	
	protected void init() {
		config = new Properties();
		Reader reader = null;
		try {
			reader = new InputStreamReader(ClassLoader.getSystemResourceAsStream(CONF_PATH), "UTF-8");
			config.load(reader);
		} catch (IOException e) {
			LOGGER.error("", e);
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}
	
	protected void initUI(String title){

		LOGGER.info("Start application {} ...", title);
		
		this.setTitle(title);  
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
		this.setSize(800, 500);
		this.setLocationRelativeTo(null);

		mainPane = new JPanel();
		mainPane.setLayout(new FlowLayout(FlowLayout.LEFT, MARGIN_LEFT, MARGIN_TOP));
		
		JPanel subPane1 = new JPanel();
		subPane1.setSize(500,500);
		GridLayout grid = new GridLayout(0, 2, MARGIN, MARGIN);
		subPane1.setLayout(grid);
			
		// 地域节点
		JLabel lbUrl = new JLabel("地域节点");
		subPane1.add(lbUrl);
		txtEndPoint = new JTextField(TXT_LEN);
		txtEndPoint.setFont(FONT_SIZE);
		txtEndPoint.setText(endPoint);
		subPane1.add(txtEndPoint);
		
		// accessKeyId
		JLabel lbUser = new JLabel("accessKeyId");
		subPane1.add(lbUser);
		txtAccessKeyId = new JTextField(TXT_LEN);
		txtAccessKeyId.setFont(FONT_SIZE);
		txtAccessKeyId.setText(accessKeyId);
		subPane1.add(txtAccessKeyId);
		
		// secretAccessKey
		JLabel lbPwd = new JLabel("secretAccessKey");
		subPane1.add(lbPwd);
		txtSecretAccessKey = new JTextField(TXT_LEN);
		txtSecretAccessKey.setFont(FONT_SIZE);
		txtSecretAccessKey.setText(secretAccessKey);
		subPane1.add(txtSecretAccessKey);
		
		// bucket
		JLabel lbBucket = new JLabel("bucket");
		subPane1.add(lbBucket);
		txtBucket = new JTextField(TXT_LEN);
		txtBucket.setFont(FONT_SIZE);
		txtBucket.setText(bucket);
		subPane1.add(txtBucket);
		
		// project
		JLabel lbProject = new JLabel("项目名称");
		subPane1.add(lbProject);
		txtProject = new JTextField(TXT_LEN);
		txtProject.setFont(FONT_SIZE);
		txtProject.setText("edpbui-v1.0.8");
		subPane1.add(txtProject);
		
		// txtInputPath
		JLabel lbInput = new JLabel("上传路径");
		subPane1.add(lbInput);
		txtInputPath = new JTextField("...");
		txtInputPath.setFont(FONT_SIZE);
		txtInputPath.setEditable(false);
		subPane1.add(txtInputPath);
		txtInputPath.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String current = txtInputPath.getText();
				current = "...".equals(current)?null:current;
				
				JFileChooser chooser = new JFileChooser(current);
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				chooser.showDialog(new JLabel(), "选择文件或目录");
				if (chooser.getSelectedFile()!=null) {
					txtInputPath.setText(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		
		subPane1.add(new JLabel());
		subPane1.add(new JLabel());
		
		// 上传文件
		JButton btnUpload = new JButton("上传文件");
		btnUpload.setFont(FONT_SIZE);
		subPane1.add(btnUpload);
		btnUpload.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				uploadOSS();
			}

		});
		
		// 查看日志
		JButton btnShowLogs = new JButton("查看日志");
		btnShowLogs.setFont(FONT_SIZE);
		subPane1.add(btnShowLogs);
		btnShowLogs.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				viewLogs();
			}
		});
		
		mainPane.add(subPane1);
		this.setContentPane(mainPane);
		this.setVisible(true);
		
		LOGGER.info("Initiated application {}", title);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
	}
	
	public void uploadOSS() {
		String endpoint 	= txtEndPoint.getText();
		String accessKeyId 	= txtAccessKeyId.getText();
		String secretAccessKey = txtSecretAccessKey.getText();
		String bucket = txtBucket.getText();
		
		String outputpath = txtProject.getText();
		String inputpath  = txtInputPath.getText();
		
		if (StringUtils.isBlank(endpoint)) {
			showMsg("地域节点不能为空");
			return;
		}
		if (StringUtils.isBlank(accessKeyId)) {
			showMsg("accessKeyId不能为空");
			return;
		}
		if (StringUtils.isBlank(secretAccessKey)) {
			showMsg("secretAccessKey不能为空");
			return;
		}
		if (StringUtils.isBlank(bucket)) {
			showMsg("Bucket不能为空");
			return;
		}
		if (StringUtils.isBlank(outputpath)) {
			showMsg("项目名称不能为空");
			return;
		}
		if (StringUtils.isBlank(inputpath) || inputpath.equals("...")) {
			showMsg("上传路径不能为空");
			return;
		}
		
		if (inputpath.length()<5) {
			showMsg("上传目录路径长度不能小于5\n"+inputpath);
			return;
		}
		
		
		OSSClientBuilder buider = new OSSClientBuilder();
		OSS oss = buider.build(endpoint, accessKeyId, secretAccessKey);
		File path = new File(inputpath);
		List<File> files = new ArrayList<>();
		if (path.isDirectory()) {
			files.addAll(FileUtils.listFiles(path, null, true));
		} else {
			files.add(path);
		}
		
		// 上传文件
		files.forEach(file->{
			String objectName = file.getAbsolutePath().replace("\\", "/");
			PutObjectProgressListener listener = new PutObjectProgressListener(objectName);
			
			objectName = path.isDirectory()?objectName.replaceFirst(path.getAbsolutePath().replace("\\", "/"), outputpath)
					: objectName.replaceFirst(path.getParent().replace("\\", "/"), outputpath);
			
			PutObjectRequest req = new PutObjectRequest(bucket, objectName, file).<PutObjectRequest>withProgressListener(listener);
			oss.putObject(req);
			
		});
		oss.shutdown();
		LOGGER.info("Upload finished");
		showMsg("上传完毕");
	}
	
	protected void viewLogs() {
		showMsg(config==null?"":config.getProperty("log4j.appender.R.File"));
	}
	
	protected void showMsg(String msg) {
		JOptionPane.showMessageDialog(new JFrame(), msg, "提示", JOptionPane.INFORMATION_MESSAGE);
	}

}
