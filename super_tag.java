
import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.geom.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.imageio.*;
import java.io.*;

public class super_tag
{
	public static void main(String argu[])
	{
		EventQueue.invokeLater(new Runnable(){
			public void run()
			{
				int refresh=1000;
				JFrame frame=new main_frame(refresh);
				frame.setTitle("super tag?");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setVisible(true);
				frame.pack();

			}
		});
	}
}

class main_frame extends JFrame
{
	private int refresh;
	private JPanel jp;
	private instruction_t instruction;
	main_frame(int refresh_)
	{
		jp=new JPanel();
		refresh=refresh_;
		instruction=new instruction_t(this,jp);
		add(instruction);
	}
	public void in_game_stage()
	{
		getContentPane().remove(instruction);
		add(new game_compos(this,refresh,jp));
		revalidate();
		pack();
	}
}

class game_compos extends JComponent
{
	public Dimension getPreferredSize(){return new Dimension(680,680);}
	private map_utilities map;
	private roles_manager roles;
	private int refresh;
	private main_frame frame;
	private static JPanel jp;
	private boolean if_restart;
	private int best_score;
	game_compos(main_frame frame_,int refresh_,JPanel jp_)
	{
		jp=jp_;
		frame=frame_;
		refresh=refresh_;
		best_score=0;
		restart();
		if_restart=false;
	}
	public void restart()
	{
		jp.getInputMap().clear();
		jp.getActionMap().clear();
		jp.getInputMap().put(KeyStroke.getKeyStroke("N"),"restart");
		jp.getActionMap().put("restart",new AbstractAction(){
            public void actionPerformed(ActionEvent event)
            {
				if_restart=true;
			}
        });
		map=new map_utilities();
		roles=new roles_manager(refresh,frame,map,jp);
		revalidate();
		if(best_score!=0)
			map.set_best_score(best_score);
	}
	public void paintComponent(Graphics g)
	{
		if(if_restart)
		{
			if_restart=false;
			restart();
		}
		map.draw(g);
		roles.draw(g);
		if(map.get_score()>best_score)
		{
			best_score=map.get_score();
			map.set_best_score(map.get_score());
		}
		try
		{
			TimeUnit.MICROSECONDS.sleep(refresh);
		}
		catch(InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
		repaint();
	}
}

class instruction_t extends JComponent
{
	public Dimension getPreferredSize(){return new Dimension(680,680);}
	private main_frame frame;
	private JPanel jp;
	public instruction_t(main_frame frame_,JPanel jp_)
	{
		frame=frame_;
		jp=jp_;
		frame.add(jp);
		set_keyStrokes();
	}
	public void paintComponent(Graphics g)
	{
		Graphics2D g2=(Graphics2D)g;
		try
		{
			g2.drawImage(ImageIO.read(new File("pics/instruction.png")),0,0,null);
		}
		catch(IOException e)
		{
			System.out.println("instruction image loading problem");
			System.exit(1);
		}
	}
	public void set_keyStrokes()
	{
		InputMap imap=jp.getInputMap();
		imap.put(KeyStroke.getKeyStroke("N"),"start");
        ActionMap amap=jp.getActionMap();
		amap.put("start",new AbstractAction(){
            public void actionPerformed(ActionEvent event)
            {
				frame.in_game_stage();
			}
        });
	}
}
