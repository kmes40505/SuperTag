
import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.geom.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.imageio.*;
import java.io.*;

public class map
{
	public static void main(String argu[])
	{
		map_utilities test=new map_utilities();
		test.debug_print_map(test.get_layout(),test.get_x_bound(),test.get_y_bound());
	}
}

class paver
{
	private int x,y;
	private paver next;
	paver(int x_, int y_)
	{
		x=x_;
		y=y_;
		next=null;
	}
	public int get_x()
	{
		return x;
	}
	public int get_y()
	{
		return y;
	}
	public paver get_next()
	{
		return next;
	}
	public void set_next(paver input)
	{
		next=input;
	}
}
class layout_funcs_
{
	ArrayList<paver> road_list;
	int layout[][];
	int x_bound,y_bound;
	layout_funcs_(ArrayList<paver> road_list_,int layout_[][],int x_bound_,int y_bound_)
	{
		road_list=road_list_;
		layout=layout_;
		x_bound=x_bound_;
		y_bound=y_bound_;
	}
	//1=left,2=right,3=up,4=down
	public int find_dir(paver origin,paver target)
	{
		if(target.get_x()-origin.get_x()==1)
			return 2;
		if(target.get_x()-origin.get_x()==-1)
			return 1;
		if(target.get_y()-origin.get_y()==1)
			return 4;
		if(target.get_y()-origin.get_y()==-1)
			return 3;
		return -1;
	}
	//int wall_road_unuse: 1=road, 0=wall, -1=unused.
	public paver check_sides(paver input,int wall_road_unused)
	{
		paver list=null,temp=null;
		if(input.get_x()!=0&&layout[input.get_x()-1][input.get_y()]==wall_road_unused)
		{
			temp=new paver(input.get_x()-1,input.get_y());
			temp.set_next(list);
			list=temp;
		}
		if(input.get_x()!=x_bound-1&&layout[input.get_x()+1][input.get_y()]==wall_road_unused)
		{
			temp=new paver(input.get_x()+1,input.get_y());
			temp.set_next(list);
			list=temp;
		}
		if(input.get_y()!=0&&layout[input.get_x()][input.get_y()-1]==wall_road_unused)
		{
			temp=new paver(input.get_x(),input.get_y()-1);
			temp.set_next(list);
			list=temp;
		}
		if(input.get_y()!=y_bound-1&&layout[input.get_x()][input.get_y()+1]==wall_road_unused)
		{
			temp=new paver(input.get_x(),input.get_y()+1);
			temp.set_next(list);
			list=temp;
		}
		return list;
	}

	public void pave_road(paver input)
	{
		paver target=check_sides(input,-1);
		while(target!=null)
		{
			paver temp=check_sides(target,1);
			if(temp!=null&&temp.get_x()==input.get_x()&&temp.get_y()==input.get_y())
				temp=temp.get_next();
			if(temp!=null)
				layout[target.get_x()][target.get_y()]=0;
			target=target.get_next();
		}
		layout[input.get_x()][input.get_y()]=1;
		road_list.add(input);
	}
	
	public int count_list(paver input)
	{
		paver temp=input;
		int count=0;
		while(temp!=null)
		{
			temp=temp.get_next();
			count++;
		}
		return count;
	}
	
	public paver list_goto(paver input,int position)
	{
		while(position!=0)
		{
			input=input.get_next();
			position--;
		}
		return input;
	}
}
class map_utilities
{
	private Image grass,wall;
	private Rectangle2D background;
	//layout: 1=road, 0=wall, -1=unused
	private static int layout[][];
	private static int x_bound,y_bound;
	private int score;
	private static boolean caught;
	private static int best_score;
	public int get_score()
	{
		return score;
	}
	public void set_score(int input)
	{
		score=input;
	}
	public static int get_best_score()
	{
		return best_score;
	}
	public void set_best_score(int input)
	{
		best_score=input;
	}
	public static void set_caught(boolean input)
	{
		caught=input;
	}
	public static boolean get_caught()
	{
		return caught;
	}
	public static int get_x_bound()
	{
		return x_bound;
	}
	
	public static int get_y_bound()
	{
		return y_bound;
	}
	
	public static int[][] get_layout()
	{
		return layout;
	}
	
	public map_utilities()
	{
		try
		{
			grass=ImageIO.read(new File("pics/grass.png"));
			wall=ImageIO.read(new File("pics/wall.png"));
		}
		catch(IOException e)
		{
		
		}
		background=new Rectangle2D.Double(0,0,680,680);
		x_bound=22;
		y_bound=21;
		layout=new int[x_bound][y_bound];
		road_list=new ArrayList<>();
		score=0;
		best_score=0;
		caught=false;
		layout_create();
	}
	
	public void draw(Graphics g)
	{
		Graphics2D g2=(Graphics2D)g;
		g2.setPaint(Color.BLACK);
		g2.fill(background);
		g2.draw(background);
		if(!caught)
			score++;
		Font f=new Font("serif",Font.BOLD,36);
        g2.setFont(f);
        g2.setPaint(Color.RED);
        g2.drawString("Score: "+score,(float)20,(float)670);
		g2.drawString("Best Score: "+best_score,(float)300,(float)670);
		draw_layout(g);
	}
	
	public void draw_layout(Graphics g)
	{
		int x=0,y=0;
		Graphics2D g2=(Graphics2D)g;
		while(x<x_bound)
		{
			while(y<y_bound)
			{
				if(layout[x][y]==1)
					g2.drawImage(grass,x*30+10,y*30+10,null);
				else
					g2.drawImage(wall,x*30+10,y*30+10,null);
				y++;
			}
			x++;
			y=0;
		}
	}
	
	static ArrayList<paver> road_list;

	public static ArrayList<paver> get_road_list()
	{
		return road_list;
	}
	
	public static int[] get_rand_road()
	{
		int ret_val[]=new int[2];
		int rand=(int)(Math.random()*road_list.size());
		paver target=road_list.get(rand);
		ret_val[0]=target.get_x();
		ret_val[1]=target.get_y();
		return ret_val;
	}
	
	public static void debug_print_map(int layout[][],int x_bound,int y_bound)
	{
		int x=0,y=0;
		int first_line=0;
		while(first_line<x_bound)
		{
			if(first_line<10)
				System.out.print(first_line+"  ");
			else
				System.out.print(first_line+" ");
			first_line++;
		}
		System.out.print("\n");
		while(y<y_bound)
		{
			while(x<x_bound)
			{
				if(layout[x][y]==-1)
					System.out.print("E  ");
				else
					System.out.print(layout[x][y]+"  ");
				x++;
			}
			System.out.println(y);
			y++;
			x=0;
		}
	}
	
	void layout_create()
	{
		class wall_t
		{
			private int x,y;
			wall_t(int x_,int y_)
			{
				x=x_;
				y=y_;
			}
			public int get_x()
			{
				return x;
			}
			public int get_y()
			{
				return y;
			}
		}
		layout_funcs_ layout_funcs=new layout_funcs_(road_list,layout,x_bound,y_bound);
		int countx=0,county=0;
		paver head=new paver((int)(Math.random()*x_bound),(int)(Math.random()*y_bound)),tail=head;
		paver potentially_unused=null,potential_scanner=null;
		paver round_trip=null,round_trip_tail=null;
		int num_round_trip=0;
		while(countx!=x_bound)
		{
			while(county!=y_bound)
			{
				layout[countx][county]=-1;
				county++;
			}
			countx++;
			county=0;
		}
		
		do
		{
			layout_funcs.pave_road(head);
			while(head!=null)//this is for normal runs
			{
				paver opening=layout_funcs.check_sides(head,-1);
				
				//count the number of paving options
				
				paver temp=opening;
				if(opening==null)
				{
					temp=head;
					head=head.get_next();
					temp.set_next(round_trip);
					if(round_trip==null)
						round_trip_tail=temp;
					round_trip_tail.set_next(temp);
					round_trip=temp;
					num_round_trip++;
					continue;
				}
				int num_opening=layout_funcs.count_list(opening);
				int next_dir=(int)(Math.random()*num_opening);
				while(opening!=null)
				{
					if(next_dir==0)
					{
						layout_funcs.pave_road(opening);
						tail.set_next(opening);
						tail=tail.get_next();
						next_dir--;
					}
					else
					{
						next_dir--;
						if((int)(Math.random()*15)==0)
						{
							layout_funcs.pave_road(opening);
							tail.set_next(opening);
							tail=tail.get_next();
						}
						else
						{
							if(potentially_unused==null)
							{
								potentially_unused=potential_scanner=opening;
							}
							else
							{
								potential_scanner.set_next(opening);
								potential_scanner=potential_scanner.get_next();
							}
						}
					}
					opening=opening.get_next();
					if(potential_scanner!=null)
						potential_scanner.set_next(null);
					tail.set_next(null);
				}
				head=head.get_next();
			}
			while(potentially_unused!=null&&layout[potentially_unused.get_x()][potentially_unused.get_y()]!=-1)
			{
				potentially_unused=potentially_unused.get_next();
			}
			if(potentially_unused==null)
				break;
			head=tail=potentially_unused;
			potentially_unused=potentially_unused.get_next();
			head.set_next(null);
		}while(true);//this is for potentially unused
		
		int num_round_trip_open=(int)(num_round_trip/8*3+1);
		while(num_round_trip_open!=0)
		{
			int rand=(int)(Math.random()*num_round_trip_open);
			round_trip=layout_funcs.list_goto(round_trip,rand);
			paver target=round_trip.get_next();
			round_trip.set_next(round_trip.get_next().get_next());
			target=layout_funcs.check_sides(target,0);
			if(target==null)
				continue;
			int num_target=layout_funcs.count_list(target);
			rand=(int)(Math.random()*num_target);
			target=layout_funcs.list_goto(target,rand);
			layout[target.get_x()][target.get_y()]=1;
			road_list.add(target);
			num_round_trip_open--;
		}
	}
}

