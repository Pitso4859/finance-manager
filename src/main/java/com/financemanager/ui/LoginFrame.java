package com.financemanager.ui;

import com.financemanager.app.AppServices;
import com.financemanager.model.User;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public final class LoginFrame extends JFrame {
    public interface LoginListener { void onLogin(User user); }
    private final AppServices services;
    private final JTextField email = new JTextField(24);
    private final JPasswordField password = new JPasswordField(24);

    public LoginFrame(AppServices services, LoginListener listener) {
        super("Finance Manager"); this.services=services;
        setDefaultCloseOperation(EXIT_ON_CLOSE); setMinimumSize(new Dimension(760,500)); setContentPane(build(listener));
        pack(); setLocationRelativeTo(null);
    }
    private JPanel build(LoginListener listener){
        JPanel root=new JPanel(new GridBagLayout());root.setBackground(UiTheme.BACKGROUND);
        JPanel card=UiTheme.card();card.setLayout(new BoxLayout(card,BoxLayout.Y_AXIS));card.setPreferredSize(new Dimension(420,390));
        JLabel title=new JLabel("Finance Manager");title.setFont(UiTheme.HEADING);title.setForeground(UiTheme.TEXT);title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle=new JLabel("Secure personal finance desktop application");subtitle.setForeground(UiTheme.MUTED);subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);card.add(Box.createVerticalStrut(4));card.add(subtitle);card.add(Box.createVerticalStrut(28));
        card.add(label("Email"));card.add(Box.createVerticalStrut(6));email.setMaximumSize(new Dimension(Integer.MAX_VALUE,34));card.add(email);card.add(Box.createVerticalStrut(14));
        card.add(label("Password"));card.add(Box.createVerticalStrut(6));password.setMaximumSize(new Dimension(Integer.MAX_VALUE,34));card.add(password);card.add(Box.createVerticalStrut(22));
        JButton login=UiTheme.primaryButton("Sign In");login.setAlignmentX(Component.LEFT_ALIGNMENT);login.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));card.add(login);card.add(Box.createVerticalStrut(12));
        JButton register=UiTheme.secondaryButton("Create Account");register.setAlignmentX(Component.LEFT_ALIGNMENT);register.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));card.add(register);
        login.addActionListener(e->authenticate(listener));password.addActionListener(e->authenticate(listener));register.addActionListener(e->showRegistration());
        root.add(card);return root;
    }
    private JLabel label(String text){JLabel l=new JLabel(text);l.setForeground(UiTheme.TEXT);l.setAlignmentX(Component.LEFT_ALIGNMENT);return l;}
    private void authenticate(LoginListener listener){try{User user=services.auth.login(email.getText(),password.getPassword());listener.onLogin(user);dispose();}
        catch(RuntimeException ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Sign in failed",JOptionPane.ERROR_MESSAGE);}}
    private void showRegistration(){
        JTextField name=new JTextField();JTextField mail=new JTextField();JPasswordField pass=new JPasswordField();JPasswordField confirm=new JPasswordField();
        JPanel panel=new JPanel(new GridLayout(0,1,4,4));panel.add(new JLabel("Full name"));panel.add(name);panel.add(new JLabel("Email"));panel.add(mail);panel.add(new JLabel("Password"));panel.add(pass);panel.add(new JLabel("Confirm password"));panel.add(confirm);
        int result=JOptionPane.showConfirmDialog(this,panel,"Create Account",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
        if(result==JOptionPane.OK_OPTION){try{services.auth.register(name.getText(),mail.getText(),pass.getPassword(),confirm.getPassword());JOptionPane.showMessageDialog(this,"Account created successfully. You can now sign in.","Account created",JOptionPane.INFORMATION_MESSAGE);email.setText(mail.getText());}
            catch(RuntimeException ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Could not create account",JOptionPane.ERROR_MESSAGE);}}
    }
}
