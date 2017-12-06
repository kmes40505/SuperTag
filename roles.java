
import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.geom.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.imageio.*;
import java.io.*;

class roles_manager
{
	private static players player;
	private static monsters monster;
	private static JPanel jp;
	private static int layout[][];
	private static int dir_layout[][];	
	private static map_utilities map;
	private static closest_path dir_info;
	private static int milli_monster_speed;
	private final static int player_speed=5000;
	public roles_manager(int refresh,main_frame frame,map_utilities map_,JPanel jp_)
	{
		jp=jp_;
		milli_monster_speed=150000;
		player=new players("char",refresh,player_speed,jp,frame,map_);
		monster=new monsters("monster",refresh,milli_monster_speed,jp,frame);
		map=map_;
		layout=map.get_layout();
		dir_info=new closest_path(layout,map.get_x_bound(),map.get_y_bound());
		dir_layout=dir_info.get_layout();
	}
	
	public static void draw(Graphics g)
	{
		dir_info.reset(map.get_layout());
		dir_info.ripple(player.get_position()[0],player.get_position()[1],monster.get_position()[0],monster.get_position()[1]);
		check_caught();
		move_roles(g,player);
		if(map.get_caught()&&monster.get_at_position()&&player.get_position()[0]==monster.get_position()[0]&&player.get_position()[1]==monster.get_position()[1])
			monster.draw(g);
		else
			move_roles(g,monster);
		if(milli_monster_speed<(player_speed-1000)*1000)
		{
			milli_monster_speed+=10;
			monster.set_interval(milli_monster_speed/1000);
		}
	}
	private static void check_caught()
	{
		int monster_posi[]=monster.get_real_posi();
		int player_posi[]=player.get_real_posi();
		if(monster_posi[0]<player_posi[0]+30&&monster_posi[0]>player_posi[0]-30&&monster_posi[1]==player_posi[1])
			map.set_caught(true);
		if(monster_posi[1]<player_posi[1]+30&&monster_posi[1]>player_posi[1]-30&&monster_posi[0]==player_posi[0])
			map.set_caught(true);
	}
	private static void move_roles(Graphics g,roles_t target)
	{
		int new_offset=0;
		if(target.get_waiting()!=0)
			target.set_waiting(target.get_waiting()-1);
		else
		{
			if(target.get_type()==1&&!((players)target).is_moving())
			{
				target.draw(g);
				target.set_offset(30);
				return;
			}
			if(target.get_type()==2&&((monsters)target).get_at_position())
			{
				target.draw(g);
				target.set_dir(dir_layout[target.get_position()[0]][target.get_position()[1]]-1);
				target.move_position();
				target.set_waiting(target.get_interval());
				((monsters)target).set_at_position(false);
				target.set_offset(0);
				return;
			}
			new_offset=(target.get_offset()+1)%30;
			target.set_offset(new_offset);
			target.set_waiting(target.get_interval());
		}
		Graphics2D g2=(Graphics2D) g;
		//0=left,1=right,2=up,3=down
		int real_position[]=target.get_real_posi();
		if(target.get_dir()==0)
			g2.drawImage(target.get_char_left(),real_position[0],real_position[1],null);
		if(target.get_dir()==1)
			g2.drawImage(target.get_char_right(),real_position[0],real_position[1],null);
		if(target.get_dir()==2)
			g2.drawImage(target.get_char_up(),real_position[0],real_position[1],null);
		if(target.get_dir()==3)
			g2.drawImage(target.get_char_down(),real_position[0],real_position[1],null);
		if(new_offset==29)
		{
			if(target.get_type()==1)
			{
				((players)target).set_moving(false);
			}
			if(target.get_type()==2)
			{
				((monsters)target).set_at_position(true);
			}
		}
	}
	
	public static boolean not_wall(int dir,int position[])
	{
		int x=0,y=0;
		if(dir==0)
		{
			if(position[0]-1<0)
				return false;
			x=-1;
		}
		if(dir==1)
		{
			if(position[0]+1==map.get_x_bound())
				return false;
			x=1;
		}
		if(dir==2)
		{
			if(position[1]-1<0)
				return false;
			y=-1;
		}
		if(dir==3)
		{
			if(position[1]+1==map.get_y_bound())
				return false;
			y=1;
		}
		if(layout[position[0]+x][position[1]+y]==1)
			return true;
		return false;
	}

}

class monsters extends roles_t
{
	private boolean at_position;
	public boolean get_at_position()
	{
		return at_position;
	}
	public void set_at_position(boolean input)
	{
		at_position=input;
	}
	public monsters(String argu, int refresh_,int speed, JPanel jp, main_frame frame)
	{
		super(argu,refresh_,speed,jp,frame,2,3);
		at_position=true;
	}
}

class players extends roles_t
{
	private boolean moving;
	private map_utilities map;
	public boolean is_moving()
	{
		return moving;
	}
	
	public void set_moving(boolean input)
	{
		moving=input;
	}

	public players(String argu,int refresh_,int speed,JPanel jp,main_frame frame,map_utilities map_)
	{
		super(argu,refresh_,speed,jp,frame,1,3);
		set_keyStroke(jp);
		moving=false;
		map=map_;
	}

	private void set_keyStroke(JPanel jp)
	{
		InputMap imap=jp.getInputMap();
        imap.put(KeyStroke.getKeyStroke("W"),"up");
        imap.put(KeyStroke.getKeyStroke("S"),"down");
		imap.put(KeyStroke.getKeyStroke("A"),"left");
		imap.put(KeyStroke.getKeyStroke("D"),"right");
        ActionMap amap=jp.getActionMap();
		amap.put("left",new AbstractAction(){
            public void actionPerformed(ActionEvent event)
            {
				key_action(0);
			}
        });
		amap.put("right",new AbstractAction(){
            public void actionPerformed(ActionEvent event)
            {
				key_action(1);
			}
        });
        amap.put("up",new AbstractAction(){
            public void actionPerformed(ActionEvent event)
            {
				key_action(2);
			}
        });
        amap.put("down",new AbstractAction(){
            public void actionPerformed(ActionEvent event)
            {
				key_action(3);
            }
        });
	}
	
	private void key_action(int input_dir)
	{
		if(map.get_caught())
			return;
		if(!moving)
		{
			super.set_dir(input_dir);
			if(roles_manager.not_wall(super.get_dir(),super.get_position()))
			{
				super.move_position();
				moving=true;
			}
		}
	}
}

class roles_t
{
	
	private Image char_up,char_down,char_left,char_right;
	private int position[];
	private int interval;
	private int waiting;
	private int dir;
	//0=left,1=right,2=up,3=down
	private int offset;
	private int refresh;
	private int type;
	//1=players, 2=ghost
	
	public roles_t(String argu,int refresh_,int speed,JPanel jp,main_frame frame,int type_,int dir_)
	{
		try
		{
			char_up=ImageIO.read(new File("pics/"+argu+"_up.png"));
			char_down=ImageIO.read(new File("pics/"+argu+"_down.png"));
			char_left=ImageIO.read(new File("pics/"+argu+"_left.png"));
			char_right=ImageIO.read(new File("pics/"+argu+"_right.png"));
		}
		catch(IOException e)
		{
			System.out.println("char image loading problem");
			System.exit(1);
		}
		position=map_utilities.get_rand_road();
		refresh=refresh_;
		set_interval(speed);
		waiting=interval;
		offset=0;
		type=type_;
		dir=dir_;
	}
	
	public void move_position()
	{
		if(dir==0)
			position[0]=position[0]-1;
		if(dir==1)
			position[0]=position[0]+1;
		if(dir==2)
			position[1]=position[1]-1;
		if(dir==3)
			position[1]=position[1]+1;
	}
	
	public int get_type()
	{
		return type;
	}
	
	public void set_refresh(int input)
	{
		refresh=input;
	}
	
	public int get_offset()
	{
		return offset;
	}
	
	public void set_offset(int input)
	{
		offset=input;
	}
	
	public int get_dir()
	{
		return dir;
	}
	
	public void set_dir(int input)
	{
		dir=input;
	}
	
	public Image get_char_up()
	{
		return char_up;
	}
	
	public Image get_char_down()
	{
		return char_down;
	}
	
	public Image get_char_left()
	{
		return char_left;
	}
	
	public Image get_char_right()
	{
		return char_right;
	}
	
	public int[] get_position()
	{
		return position;
	}
	
	public int get_interval()
	{
		return interval;
	}
	
	//input=moving how many pixels per second
	public void set_interval(int input)
	{
		interval=1000000/refresh/input;
	}
	
	public int get_waiting()
	{
		return waiting;
	}
	
	public void set_waiting(int input)
	{
		waiting=input;
	}
	
	public void draw(Graphics g)
	{
		Graphics2D g2=(Graphics2D)g;
		if(dir==0)
			g2.drawImage(char_left,position[0]*30+10,position[1]*30+10,null);
		if(dir==1)
			g2.drawImage(char_right,position[0]*30+10,position[1]*30+10,null);
		if(dir==2)
			g2.drawImage(char_up,position[0]*30+10,position[1]*30+10,null);
		if(dir==3)
			g2.drawImage(char_down,position[0]*30+10,position[1]*30+10,null);
	}
	public int[] get_real_posi()
	{
		int posi[]=new int[2];
		if(get_dir()==0)
		{
			posi[0]=(position[0]+1)*30+10-get_offset();
			posi[1]=position[1]*30+10;
		}
		if(get_dir()==1)
		{
			posi[0]=(position[0]-1)*30+10+get_offset();
			posi[1]=position[1]*30+10;
		}
		if(get_dir()==2)
		{
			posi[0]=position[0]*30+10;
			posi[1]=(position[1]+1)*30+10-get_offset();
		}
		if(get_dir()==3)
		{
			posi[0]=position[0]*30+10;
			posi[1]=(position[1]-1)*30+10+get_offset();
		}
		return posi;
	}
}
