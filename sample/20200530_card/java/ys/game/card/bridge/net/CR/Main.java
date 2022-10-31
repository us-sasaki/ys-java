import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class Main extends Frame implements ActionListener, NetEventListener {
	
	NetClient	client;
	
	TextField	field	= new TextField(40);
	LogArea		log		= new LogArea();
	
	public Main() {
		super("Chat Window");
		
		try {
			client = new NetClient("localhost", 7878);
			client.setNetEventListener(this);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		setLayout(new GridLayout(2, 1));
		add(field);
		add(log);
		
		field.addActionListener(this);
		
		pack();
		show();
	}
	
	public void actionPerformed(ActionEvent e) {
		client.getPrintWriter().println(field.getText());
		client.getPrintWriter().flush();
		field.setText("");
//		log.addLine(field.getText());
//		field.setText("");
	}
	
	public void commandInvoked(String command) {
		log.addLine(command);
	}
	
	public static void main(String[] args) throws Exception {
		
		new Main();
	}
}
