
import java.lang.*;

public class closest_path
{
	public static void main(String argu[])
	{
		map_utilities map=new map_utilities();
		int player[],ghost[];
		player=map_utilities.get_rand_road();
		ghost=map_utilities.get_rand_road();
		closest_path testing=new closest_path(map.get_layout(),map.get_x_bound(),map.get_y_bound());
		testing.ripple(player[0],player[1],ghost[0],ghost[1]);
		System.out.printf("Player loca: %d %d\nGhost loca:  %d %d\n",player[0],player[1],ghost[0],ghost[1]);
		map_utilities.debug_print_map(testing.get_layout(),testing.get_bound_x(),testing.get_bound_y());
	}
	
	private int layout[][];
	//1=left,2=right,3=up,4=down,0=wall,-1=unused,5=char
	private int bound_x;
	private int bound_y;
	private layout_funcs_ layout_funcs;
	node player,ghost;
	public int get_bound_x()
	{
		return bound_x;
	}
	public int get_bound_y()
	{
		return bound_y;
	}
	public int[][] get_layout()
	{
		return layout;
	}
	public closest_path(int layout_[][],int bound_x_,int bound_y_)
	{
		layout=new int[bound_x_][bound_y_];
		bound_x=bound_x_;
		bound_y=bound_y_;
		layout_funcs=new layout_funcs_(null,layout,bound_x,bound_y);
		reset(layout_);
	}
	public void reset(int layout_[][])
	{
		int x=0;
		int y=0;
		while(x<bound_x)
		{
			while(y<bound_y)
			{
				//copy map and change road to unused for further process
				layout[x][y]=-layout_[x][y];
				y++;
			}
			y=0;
			x++;
		}
		head=null;
		tail=null;
		edge_list=null;
		edge_list_tail=null;
	}
	private void pave_dir(int x,int y,int move_dir)
	{
		if(move_dir==1)
			layout[x-1][y]=2;
		if(move_dir==2)
			layout[x+1][y]=1;
		if(move_dir==3)
			layout[x][y-1]=4;
		if(move_dir==4)
			layout[x][y+1]=3;
	}
	public void ripple(int x,int y,int target_x, int target_y)
	{
		int distance=0;
		boolean ghost_found=false;
		node centre=new node(x,y);
		new edge(centre,0);
		while(head.get_distance()!=distance||!ghost_found)
		{
			head.set_distance(head.get_distance()+1);
			paver edge_loca=new paver(head.get_x(),head.get_y());
			paver check=layout_funcs.check_sides(edge_loca,-1);
			boolean split=false;
			boolean first=true;
			int first_dir=-1;
			if(check==null)
			{
				head=head.get_next();
				continue;
			}
			while(check!=null)
			{
				if(check.get_x()==target_x&&check.get_y()==target_y)
					ghost_found=true;
				int dir=layout_funcs.find_dir(edge_loca,check);
				pave_dir(edge_loca.get_x(),edge_loca.get_y(),dir);
				if(split)
				{
					if(first)
					{
						head.add_node(new node(head.get_x(),head.get_y()));
						first=false;
					}
					head.edge_split(dir);
				}
				else
					first_dir=dir;
				check=check.get_next();
				split=true;
			}
			head.set_position(first_dir);
			tail.set_next(head);
			tail=tail.get_next();
			head=head.get_next();
			if(head.get_distance()>distance)
				distance++;
		}
	}
	class edge
	{
		private int x,y;
		private edge next;
		private int distance;
		private int came_dir;
		public int get_came_dir()
		{
			return came_dir;
		}
		public void set_came_dir(int input)
		{
			came_dir=input;
		}
		public int get_distance()
		{
			return distance;
		}
		public void set_distance(int input)
		{
			distance=input;
		}
		public int get_x()
		{
			return x;
		}
		public int get_y()
		{
			return y;
		}
		public void set_x(int input)
		{
			x=input;
		}
		public void set_y(int input)
		{
			y=input;
		}
		public edge get_next()
		{
			return next;
		}
		public void set_next(edge input)
		{
			next=input;
		}
		public void set_position(int dir)
		{
			came_dir=dir_funcs.oppo_dir(dir);
			if(dir==1)
				x--;
			else if(dir==2)
				x++;
			else if(dir==3)
				y--;
			else if(dir==4)
				y++;
			else
			{
				System.out.println("invalid edge.set_position input");
				System.exit(1);
			}
		}
		class node_list
		{
			node current;
			node_list next;
			public node get_current()
			{
				return current;
			}
			public node_list get_next()
			{
				return next;
			}
			public void set_next(node_list input)
			{
				next=input;
			}
			public node_list(node current_)
			{
				next=null;
				current=current_;
			}
			public node_list copy()
			{
				node_list temp=new node_list(current);
				temp.set_next(next);
				return temp;
			}
			public node_list copy_list(node_list head)
			{
				node_list temp=head.copy();
				if(head.get_next()!=null)
					temp.next=copy_list(head.get_next());
				return temp;
			}
		}
		public void add_node(node input)
		{
			tail.set_next(new node_list(input));
			tail=tail.get_next();
		}
		node_list head,tail;
		public node_list get_head()
		{
			return head;
		}
		public node_list get_tail()
		{
			return tail;
		}
		public edge(node char_node,int dir)
		{
			if(char_node!=null)
			{
				head=new node_list(char_node);
				x=char_node.get_x();
				y=char_node.get_y();
			}
			else
			{
				head=null;
				x=-1;
				y=-1;
			}
			if(closest_path.get_head()==null)
			{
				closest_path.set_head(this);
				closest_path.set_tail(this);
			}
			came_dir=dir;
			tail=head;
			next=null;
			distance=0;
			add_edge_list(new edge_list_t(this));
		}
///////////////////////////////////////////////////////////////////////////////////////adding to next directly		
		public void edge_split(int dir)
		{
			edge temp=new edge(null,dir_funcs.oppo_dir(dir));
			temp.head=head.copy_list(head);
			node_list node_temp=temp.head;
			while(node_temp.next!=null)
			{
				node_temp=node_temp.get_next();
			}
			temp.tail=node_temp;
			temp.x=x;
			temp.y=y;
			temp.set_position(dir);
			temp.set_distance(distance);
			closest_path.this.get_tail().set_next(temp);
			closest_path.this.set_tail(closest_path.this.get_tail().get_next());
		}
			
		
		
			
			
	}
	
	static edge head,tail;
	public static edge get_head()
	{
		return head;
	}
	public static edge get_tail()
	{
		return tail;
	}
	public static void set_head(edge input)
	{
		head=input;
	}
	public static void set_tail(edge input)
	{
		tail=input;
	}
	class edge_list_t
	{
		edge current;
		edge_list_t next;
		edge_list_t(edge input)
		{
			current=input;
			next=null;
			if(edge_list==null)
			{
				edge_list=this;
				edge_list_tail=this;
			}
		}
		public edge get_edge()
		{
			return current;
		}
		public edge_list_t get_next()
		{
			return next;
		}
		public void set_next(edge_list_t input)
		{
			next=input;
		}
	}
	void add_edge_list(edge_list_t input)
	{
		if(edge_list_tail!=input)
		{
			edge_list_tail.set_next(input);
			edge_list_tail=edge_list_tail.get_next();
		}
	}
	private static edge_list_t edge_list,edge_list_tail;

	class node
	{
		node up, down, left, right;
		int x,y;
		public node(int x_,int y_)
		{
			up=null;
			down=null;
			left=null;
			right=null;
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
		public void set_x(int input)
		{
			x=input;
		}
		public void set_y(int input)
		{
			y=input;
		}
		public node get_up()
		{
			return up;
		}
		public node get_down()
		{
			return down;
		}
		public node get_left()
		{
			return left;
		}
		public node get_right()
		{
			return right;
		}
		public void set_right(node input)
		{
			right=input;
		}
		public void set_left(node input)
		{
			left=input;
		}
		public void set_up(node input)
		{
			up=input;
		}
		public void set_down(node input)
		{
			down=input;
		}
		public node cp_node()
		{
			node temp=new node(x,y);
			temp.up=up;
			temp.down=down;
			temp.left=left;
			temp.right=right;
			return temp;
		}
	}
}

class dir_funcs
{
	public static int oppo_dir(int input)
	{
		if(input==1)
			return 2;
		else if(input==2)
			return 1;
		else if(input==3)
			return 4;
		else if(input==4)
			return 3;
		return -1;
	}
}