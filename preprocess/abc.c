#include <fstream.h>
#include <iostream.h>
#include <conio.h>
#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#define max_gen 1000
#include <time.h>
#include <dos.h>
#define t0 400
#define neigh 9
#define max_machine in the cell 4
#define max_job  in on e machine 8
#define maxop_job 3
#define maxmac_op 3
#define max_time 480
#define max_tool 5
#define max_su 1920.0
#define max_th 80.0
#define w1 1.0
#define w2 1.0

float obj_fun(int ini_str[max_job])
{
	int i,j,k;
	int rtm[max_machine];
	int rts[max_machine];
	int trtm[max_machine]={0};
	int trts[max_machine]={0};
	float tpt_job=0.0;
	float obj_fun1,obj_fun2,tot_obj;
	int btsz[max_job]={8,9,13,6,9,10,12,13};
	float tpt[max_job]={144.0,639.0,481.0,198.0,423.0,440.0,660.0,728.0};
	float tot_wl=0.0;
	int wl[max_machine];
	float avg_wl=tot_wl/max_machine;
	float temp_obj[2][maxmac_op];
	int atm[max_machine];

int upt[max_job][maxop_job][maxmac_op]={{18,0,0,0,0,0,0,0,0},
					{25,25,0,24,0,0,22,0,0},
					{26,26,0,11,0,0,0,0,0},
					{14,0,0,19,0,0,0,0,0},
					{22,22,0,25,0,0,0,0,0},
					{16,0,0,7,7,7,21,21,0},
					{19,19,19,13,13,13,23,0,0},
					{25,25,25,7,7,0,24,0,0}};

int ts[max_job][maxop_job][maxmac_op]={{1,0,0,0,0,0,0,0,0},
				       {1,1,0,1,0,0,1,0,0},
				       {2,2,0,3,0,0,0,0,0},
				       {1,0,0,1,0,0,0,0,0},
				       {2,2,0,1,0,0,0,0,0},
				       {1,0,0,1,1,1,1,1,0},
				       {1,1,1,1,1,1,3,0,0},
				       {1,1,1,1,1,0,3,0,0}};
int mach_no[max_job][maxop_job][maxmac_op]={{3,0,0,0,0,0,0,0,0},
					    {1,4,0,4,0,0,2,0,0},
					    {4,1,0,3,0,0,0,0,0},
					    {3,0,0,4,0,0,0,0,0},
					    {2,3,0,2,0,0,0,0,0},
					    {4,0,0,4,2,3,2,1,0},
					    {3,2,4,2,3,1,4,0,0},
					    {1,2,3,2,1,0,1,0,0}};

int op_no[max_job][maxop_job]={{1,0,0},
			       {1,2,3},
			       {1,2,0},
			       {1,2,0},
			       {1,2,0},
			       {1,2,3},
			       {1,2,3},
			       {1,2,3}};
float dev_wl=0.0;
float temp;
int temp1;
int l,m,var;
int su;
int th=0;
int su1=0;
int su2=0;
int tem1=0;
int tem2=0;
int dec_var=0;
int sel_job[max_job];
int rej_job[max_job];
int count=0;
int count1=0;
int temp_su;
	
	obj_fun1=0.0;
	obj_fun2=0.0;
	
	for(i=0;i<max_job;i++)
	   {
		    tot_wl+=tpt[i];
	   }

	
	for(i=0;i<max_machine;i++)
	   {
		    rtm[i]=max_time;
	    	rts[i]=max_tool;
	    	atm[i]=max_time;
	   }



for(i=0;i<max_machine;i++)
{
	trtm[i]=rtm[i];
	trts[i]=rts[i];
}
for(i=0;i<max_job;i++)
   {
    su=0;temp_su=0;
    for(int kk=0;kk<maxmac_op;kk++)
	{
	  temp_obj[0][kk]=0.0;
	  temp_obj[1][kk]=kk;
	}
    for(int yg=0;yg<max_machine;yg++){su+=rtm[yg];}
    for(int s=0;s<max_machine;s++)
    {
	trtm[s]=rtm[s];
	trts[s]=rts[s];
    }
    for(j=0;j<maxop_job;j++)
       {
	if(op_no[ini_str[i]-1][j]>0)
	  {
	    for(k=0;k<maxmac_op;k++)
	       {
		if(mach_no[ini_str[i]-1][j][k]!=0)
		  {
		   temp_obj[0][k]=(0.5*rtm[mach_no[ini_str[i]-1][j][k]-1])+(0.5*rts[mach_no[ini_str[i]-1][j][k]-1]);
		  }
		else {temp_obj[0][k]=0;}

		if(temp_obj[0][k]<0){temp_obj[0][k]=0;}
	       }

	     for(k=0;k<maxmac_op;k++){temp_obj[1][k]=k;}
	     for(k=0;k<maxmac_op;k++)
		{
		 for(m=0;m<maxmac_op;m++)
		    {
		     if(temp_obj[0][k]>temp_obj[0][m])
		       {
			temp=temp_obj[0][k];
			temp_obj[0][k]=temp_obj[0][m];
			temp_obj[0][m]=temp;
			temp1=temp_obj[1][k];
			temp_obj[1][k]=temp_obj[1][m];
			temp_obj[1][m]=temp1;
		       }
		    }
		}

	    rtm[mach_no[ini_str[i]-1][j][temp_obj[1][0]]-1]=rtm[mach_no[ini_str[i]-1][j][temp_obj[1][0]]-1]-(upt[ini_str[i]-1][j][temp_obj[1][0]]*btsz[ini_str[i]-1]);
	    rts[mach_no[ini_str[i]-1][j][temp_obj[1][0]]-1]=rts[mach_no[ini_str[i]-1][j][temp_obj[1][0]]-1]-ts[ini_str[i]-1][j][temp_obj[1][0]];
	    tem1=mach_no[ini_str[i]-1][j][temp_obj[1][0]]-1;
	    su=0;
	    for(int yg=0;yg<max_machine;yg++){su+=rtm[yg];}
	    if(rts[tem1]<0 || su<0)
	      {
	       dec_var=1;
	       for(int s=0; s<max_machine;s++)
		{
			rtm[s]=trtm[s];
			rts[s]=trts[s];
		}
		break;
	      }

	  }//operation condition

	}//operation loop

if(dec_var!=1){sel_job[count]=ini_str[i]-1;count+=1;}
else{rej_job[count1]=ini_str[i]-1;count1+=1;}


}//loop for job number

int sys_un=0;
for(i=0;i<max_machine;i++)
   {
    wl[i]=atm[i]-rtm[i];
    sys_un+=rtm[i];
   }
for(i=0;i<max_machine;i++)
   {
    dev_wl+=abs(wl[i]-avg_wl);
   }
tpt_job=0;
for(i=0;i<count;i++)
   {

    th+=btsz[sel_job[i]];
    tpt_job+=tpt[sel_job[i]];
   }
obj_fun1=(max_su-sys_un)/(max_su);
obj_fun2=th/(max_th);
tot_obj=((w1*obj_fun1)+(w2*obj_fun2))/(w1+w2);
return(tot_obj);
}

 //************************ SIMULATED ANNEALING******************************
void main()
{
  randomize();
  clrscr();

	int i,j,k,gen,ra1,ra2,ra3,ra4,ra5,temp;
	int initial_string[max_job];
	int pert_string[max_job];
	float fitness,pert_fitness,t;
	int bstr[max_job]={0};
   for(i=0;i<max_job;i++)
   {
		initial_string[i]=i+1;
   }
		fitness=1000;

	for(i=0;i<max_job+50;i++)
	{
		ra1=rand()%max_job;
		ra2=rand()%max_job;
		ra3=rand()%max_job;
		ra4=rand()%max_job;
		ra5=rand()%max_job;
		temp=initial_string[ra1];
		initial_string[ra1]=initial_string[ra2];
		initial_string[ra2]=initial_string[ra3];
		initial_string[ra3]=initial_string[ra4];
		initial_string[ra4]=initial_string[ra5];
		initial_string[ra5]=temp;
	}

	fitness=obj_fun(initial_string);
	t=t0;
	float obj=0.00001;
	for(gen=0;gen<max_gen;gen++)
	{
		for(i=0;i<max_job;i++)
		{
		       pert_string[i]=initial_string[i];
		}
		for(i=0;i<max_job;i++)
		{
		       int r1=rand()%max_job in the cell;
		       int r2=rand()%max_job in the cell;
		       int r3=rand()%max_job in the cell;
		       int r4=rand()%max_job in the cell;
		       int r5=rand()%max_job in the cell;
		       temp=pert_string[r1];
		       pert_string[r1]=pert_string[r2];
		       pert_string[r2]=pert_string[r3];
		       pert_string[r3]=pert_string[r4];
		       pert_string[r4]=pert_string[r5];
		       pert_string[r5]=temp;
		}
			      pert_fitness=obj_fun(pert_string);
			      if(pert_fitness>=fitness)
				{
				    fitness=pert_fitness;
					for(i=0;i<max_job;i++)
					{
					   initial_string[max_job]=pert_string[max_job];
					}
				}
			      else
			      {
				      float rt=(1+rand()%999)/1000.0;
				      float p=exp(-(fitness-pert_fitness)/t);
				      if( p>rt)
				      {
					fitness=pert_fitness;
						for(i=0;i<max_job;i++)
						{
						   initial_string[max_job]=pert_string[max_job];
						}
				      }
			       }
					t=(t0/(1+log(gen+2)))+3;
					if(obj<pert_fitness)
					{
					   obj= fitness;
					   for(i=0;i<max_job;i++)
					   {
						bstr[i]=pert_string[i];
					   }
					}
					cout<<"\n";
					cout<<" obj::: "<<obj<<"\n";
					for(i=0;i<max_job;i++)
					{
						cout<<bstr[i]<<"   ";
					}
					cout<<gen<<"gen"<<"\n\n";
	}
   getch();
}	