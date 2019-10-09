package demo;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.io.*;

public class TestCode {
    public static void main(String[] arg0) throws IOException {
/* u.data     -- The full u data set, 100000 ratings by 943 users on 1682 items.
              Each user has rated at least 20 movies.  Users and items are
              numbered consecutively from 1.  The data is randomly
              ordered. This is a tab separated list of
	         user id | item id | rating | timestamp.
              The time stamps are unix seconds since 1/1/1970 UTC*/
        int[][] train_matrix = new int[943][1682];
        int[][] test_matrix = new int[943][1682];
        File filename = new File("u.data");
        InputStreamReader reader = new InputStreamReader(new FileInputStream(filename));
        BufferedReader br = new BufferedReader(reader);
        String line = "";
        line = br.readLine();
        int i=0;
        while(line != null) {
            if(i<50000) {
                String[] array = line.split("\t");
                train_matrix[Integer.parseInt(array[0]) - 1][Integer.parseInt(array[1]) - 1] = Integer.parseInt(array[2]);
                line = br.readLine();
            }else{
                String[] array = line.split("\t");
                test_matrix[Integer.parseInt(array[0]) - 1][Integer.parseInt(array[1]) - 1] = Integer.parseInt(array[2]);
                line = br.readLine();
            }
            i++;
        }
        BPR bpr3 = new BPR(train_matrix,test_matrix,0.5,0.0001,5);
        bpr3.train(5);
        bpr3.test(5);
        System.out.println(bpr3.getIndexof(1,1));
        bpr3.train(10);
        bpr3.test(5);
        System.out.println(bpr3.getIndexof(1,1));
        bpr3.train(20);
        bpr3.test(5);
        System.out.println(bpr3.getIndexof(1,1));
        BPR bpr1 = new BPR(train_matrix,test_matrix,0.5,0.0001,10);
        bpr1.train(5);
        bpr1.test(5);
        System.out.println(bpr1.getIndexof(1,1));
        bpr1.train(10);
        bpr1.test(5);
        System.out.println(bpr1.getIndexof(1,1));
        bpr1.train(20);
        bpr1.test(5);
        System.out.println(bpr1.getIndexof(1,1));
        BPR bpr2 = new BPR(train_matrix,test_matrix,0.5,0.0001,19);
        bpr2.train(5);
        bpr2.test(5);
        System.out.println(bpr2.getIndexof(1,1));
        bpr2.train(10);
        bpr2.test(5);
        System.out.println(bpr2.getIndexof(1,1));
        bpr2.train(20);
        bpr2.test(5);
        System.out.println(bpr2.getIndexof(1,1));
    }
}

public class BPR {
    private int users_num;
    private int items_num;
   /* private ArrayList<int[]> triads = new ArrayList<>();//[[u1,i1,j1],[u2,i2,j2],...]*/
    private int[][] train_matrix;
    private int[][] test_matrix;
    private double step_para;//梯度步长
    private double regular_para;//正则化参数
    private int k;//分解矩阵维度
    private double[][] W;
    private double[][] H;
    private double[][] like_list;
    private List<LinkedList<Integer>> likeSeq;
    public BPR(int[][] train_matrix,int[][] test_matrix, double step_para, double regular_para, int k){
        this.train_matrix=train_matrix;
        this.test_matrix = test_matrix;
        this.users_num=train_matrix.length;
        this.items_num=train_matrix[0].length;
        this.step_para=step_para;
        this.regular_para=regular_para;
        this.k=k;
        this.like_list = new double[users_num][items_num];
        this.init(train_matrix);
    }
    private void init(int[][] like_matrix){
        W = new double[users_num][k];
        H = new double[items_num][k];
        for(int i=0;i<users_num;i++) {
            for (int j = 0; j < k; j++) {
                W[i][j] = Math.random();
            }
        }
        for(int i=0;i<items_num;i++) {
            for (int j = 0; j < k; j++) {
                H[i][j] = Math.random();
            }
        }
/*        for(int u=0;u<users_num;u++){
            for(int i=0;i<items_num;i++){
                for(int j=i;j<items_num;j++){
                    if(like_matrix[u][i]>like_matrix[u][j]){
                        triads.add(new int[]{u,i,j});
                    }else if(like_matrix[u][j]>like_matrix[u][i]){
                        triads.add(new int[]{u,j,i});
                    }
                }
            }
        }*/
    }
    public void train(int times){
        for(int time=0;time<times;time++) {
            for (int sampleCount=0,smax=users_num*100;sampleCount<smax;sampleCount++) {
                int user, positem, negitem;
                while (true) {
                    user = (int) Math.round(Math.random() * (users_num-1));
                    positem = (int) Math.round(Math.random() * (items_num-1));
                    if(train_matrix[user][positem]==0) continue;
                    negitem = (int) Math.round(Math.random() * (items_num-1));
                    if(train_matrix[user][positem]>train_matrix[user][negitem]) break;
                }
                for(int f=0;f<k;f++){
                    double w = 1 / (1 + Math.pow(Math.E, x(user, positem) - x(user, negitem))) * (H[positem][f] - H[negitem][f]);
                    double hpos = 1 / (1 + Math.pow(Math.E, x(user, positem) - x(user, negitem))) * W[user][f];
                    double hneg = - 1 / (1 + Math.pow(Math.E, x(user, positem) - x(user, negitem))) * W[user][f];
/*                    double w=0,hpos=0,hneg=0;
                    for(int i=0;i<items_num;i++){
                        for(int j=i+1;j<items_num;j++) {
                            if (train_matrix[user][i] > train_matrix[user][j]) {
                                w += 1 / (1 + Math.pow(Math.E, x(user, i) - x(user, j))) * (H[i][f] - H[j][f]);
                            } else if (train_matrix[user][i] < train_matrix[user][j]) {
                                w += 1 / (1 + Math.pow(Math.E, x(user, j) - x(user, i))) * (H[j][f] - H[i][f]);
                            }
                        }
                    }
                    for(int u=0;u<users_num;u++) {
                        for (int j = 0; j < items_num; j++) {
                            if (train_matrix[u][positem] > train_matrix[u][j]) {
                                hpos += 1 / (1 + Math.pow(Math.E, x(u, positem) - x(u, j))) * W[u][f];
                            } else if (train_matrix[u][j] > train_matrix[u][positem]) {
                                hpos -= 1 / (1 + Math.pow(Math.E, x(u, j) - x(u, positem))) * W[u][f];
                            }
                        }
                    }
                    for(int u=0;u<users_num;u++) {
                        for (int j = 0; j < items_num; j++) {
                            if (train_matrix[u][negitem] > train_matrix[u][j]) {
                                hpos += 1 / (1 + Math.pow(Math.E, x(u, negitem) - x(u, j))) * W[u][f];
                            } else if (train_matrix[u][j] > train_matrix[u][negitem]) {
                                hpos -= 1 / (1 + Math.pow(Math.E, x(u, j) - x(u, negitem))) * W[u][f];
                            }
                        }
                    }*/

                    W[user][f] += step_para * (w + regular_para * W[user][f]);
                    H[positem][f] += step_para * (hpos + regular_para * H[positem][f]);
                    H[negitem][f] += step_para * (hneg + regular_para * H[negitem][f]);
                }
               /* while (true) {
                    int choose = (int) Math.round(Math.random() * (triads.size()-1));
                    user = triads.get(choose)[0];
                    positem = triads.get(choose)[1];
                    negitem = triads.get(choose)[2];
                    break;
                }
                for (int f = 0; f < k; f++) {
                    int w = 0;
                    int hpos = 0;
                    int hneg = 0;
                    for (int index = 0; index < triads.size(); index++) {
                        int u = triads.get(index)[0];
                        int i = triads.get(index)[1];
                        int j = triads.get(index)[2];
                        if (u == user) {
                            w += 1 / (1 + Math.pow(Math.E, x(u, i) - x(u, j))) * (H[i][f] - H[j][f]);
                        }
                        if (i == positem) {
                            hpos += 1 / (1 + Math.pow(Math.E, x(u, i) - x(u, j))) * W[u][f];
                        }
                        if (j == negitem) {
                            hneg -= 1 / (1 + Math.pow(Math.E, x(u, i) - x(u, j))) * W[u][f];
                        }
                    }
                    W[user][f] += step_para * (w + regular_para * W[user][f]);
                    H[positem][f] += step_para * (hpos + regular_para * H[positem][f]);
                    H[negitem][f] += step_para * (hneg + regular_para * H[negitem][f]);
                }*/

            }
           /* for (int user = 0; user < users_num; user++) {
                for (int f = 0; f < k; f++) {
                    int w = 0;
                    for (int index = 0; index < triads.size(); index++) {
                        int u = triads.get(index)[0];
                        if (user == u) {
                            int i = triads.get(index)[1];
                            int j = triads.get(index)[2];
                            w += 1 / (1 + Math.pow(Math.E, x(u, i) - x(u, j))) * (H[i][f] - H[j][f]);
                        }
                    }
                    W[user][f] += step_para * (w + regular_para * W[user][f]);
                }
            }
            for (int item = 0; item < items_num; item++) {
                for (int f = 0; f < k; f++) {
                    int h = 0;
                    for (int index = 0; index < triads.size(); index++) {
                        int i = triads.get(index)[1];
                        int j = triads.get(index)[2];
                        if (i == item) {
                            int u = triads.get(index)[0];
                            h += 1 / (1 + Math.pow(Math.E, x(u, i) - x(u, j))) * W[u][f];
                        } else if (j == item) {
                            int u = triads.get(index)[0];
                            h -= 1 / (1 + Math.pow(Math.E, x(u, i) - x(u, j))) * W[u][f];
                        }
                    }
                    H[item][f] += step_para * (h + regular_para * H[item][f]);
                }
            }*/
        }
    }
    public void test(int num){
        getSeq(num);
        System.out.println("test:");
        double totalAUC = 0;
        for(int u=0;u<users_num;u++){
            double M=0,N=0;
            for(int i=0;i<num;i++){
                if(test_matrix[u][likeSeq.get(u).get(i)]==0){
                    N++;
                }else{
                    M++;
                }
            }
            totalAUC += M/(M+N);
        }
        System.out.println("AUC:"+totalAUC/users_num);
    }
    public int getIndexof(int user,int index){
        return likeSeq.get(user).get(index);
    }
    private void getSeq(int num){
        likeSeq = new ArrayList<LinkedList<Integer>>();
        for(int u =0;u<users_num;u++){
            for(int i =0;i<items_num;i++) {
                like_list[u][i]=0;
                for (int f = 0; f < k; f++) {
                    like_list[u][i]+=W[u][f]*H[i][f];
                }
            }
            LinkedList<Integer> list = new LinkedList<Integer>();
            for(int j=0;j<num;j++) {
                int index=0;
                for (int i = 1; i < items_num; i++) {
                    if (like_list[u][i] > like_list[u][index] && !list.contains(i) && train_matrix[u][i]==0) {
                        index = i;
                    }
                }
                list.add(j, index);
            }
            likeSeq.add(list);
        }
    }
    private double x(int u,int i){
        int x=0;
        for(int f=0;f<k;f++){
            x+=W[u][f]*H[i][f];
        }
        return x;
    }
}
