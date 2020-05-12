import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleMovieRecommender implements BaseMovieRecommender {
//
    //howcanidothisthisisshitsomuchshitthattheshititselfembarassbythisshittyshit

    HashMap<Integer,Movie> mMap = new HashMap<>();
    HashMap<Integer,User> uMap = new HashMap<>();

    double[][] similarity;
    double[][] ratingMatrix;
    @Override
    public Map<Integer, Movie> loadMovies(String movieFilename) {
        Pattern pattern1 = Pattern.compile("(\\d+),([a-zA-Z ]+)\\s\\((\\d{4})\\),([a-zA-Z|]+)");
        Pattern pattern2 = Pattern.compile("(\\d+),\"?([a-zA-Z,\\W? ]+)\\s\\((\\d{4})\\)\"?,([a-zA-Z|?]+)");
        Pattern pattern3 = Pattern.compile("^([\\d]+),\"?([\\S ]+)\\s\\(([\\d]{4})\\)\"?,(.*)$");
        String line = null;


        Map<Integer,Movie> rtn = new HashMap<>();

        try {
            BufferedReader bf = new BufferedReader(new FileReader(movieFilename));

            while ((line = bf.readLine()) != null)
            {
                Matcher m = pattern3.matcher(line);
                //int id =Integer.parseInt(m.group(2));
                if(m.matches()){
                    Movie temp = new Movie(Integer.parseInt(m.group(1)),m.group(2),Integer.parseInt(m.group(3)));
                    for (String i:m.group(4).split("\\|")) {
                        temp.addTag(i);
                    }
                    rtn.put(Integer.parseInt(m.group(1)),temp);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        for (Movie m : rtn.values()) {
//            if(m==null){
//                System.out.println(m);
//            }
//            System.out.println(m);
//        }
        return rtn;
    }

    @Override
    public Map<Integer, User> loadUsers(String ratingFilename) {
        HashMap<Integer,User> user = new HashMap<>();
        String line = null;
        int uid=0;
        int mid=0;
        double rate=0;
        long time = 0;
        try {
            BufferedReader bf = new BufferedReader(new FileReader(ratingFilename));

            bf.readLine();
            while ((line = bf.readLine())!=null){
                String[] spit = line.split(",");
                uid = Integer.parseInt(spit[0]);
                mid = Integer.parseInt(spit[1]);
                rate = Double.parseDouble(spit[2]);
                time = Long.parseLong(spit[3]);
                User temp = new User(uid);
                    if(!(user.containsKey(uid))){
                        user.put(uid,temp);
                    }
                    user.get(uid).addRating(mMap.get(mid),rate,time);
//                    if(mMap.get(mid)==null){
//                        System.out.println(mMap.get(mid));
//                    }
//
//
//                System.out.println(mMap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        for (User m : user.values()) {
//            System.out.println(m);
//        }
        return user;
    }


    @Override
    public void loadData(String movieFilename, String userFilename) {
        mMap = (HashMap<Integer, Movie>) loadMovies(movieFilename);
        uMap = (HashMap<Integer, User>) loadUsers(userFilename);
    }

    @Override
    public Map<Integer, Movie> getAllMovies() {
        return mMap;
    }

    @Override
    public Map<Integer, User> getAllUsers() {
        return uMap;
    }

    @Override
    public void trainModel(String modelFilename) {
        int numUser=0;
        int numMovie=0;
        Map<Integer,Integer> uNum = new HashMap();
        Map<Integer,Integer>  mNum = new HashMap();

        for(Integer i:uMap.keySet()){
            uNum.put(numUser,i);
        }
        for (Integer i:mMap.keySet()){
            mNum.put(numMovie,i);
        }
        ratingMatrix = new double[uMap.size()][mMap.size()+1];

        for(int i=0;i<numUser;i++){
            for (int j=0;j<numMovie;j++){
                if(uMap.get(uNum.get(i)).ratings.get(mNum.get(j))!=null){
                    ratingMatrix[i][j] = uMap.get(uNum.get(i)).ratings.get(mNum.get(j)).rating;
                }
                else ratingMatrix[i][j] = 0.0;
            }
        }

        similarity = new double[uMap.size()][uMap.size()];
        computeSimilar();
        PrintWriter prin=null;
        try {
            prin = new PrintWriter(modelFilename);
            prin.println("@Num_user "+numUser);
            prin.println("@User_Map "+uNum);
            prin.println("@NUM_MOVIES "+numMovie);
            prin.println("@MOVIE_MAP "+mNum);
            prin.println("@RATING_MATRIX");
            for (int i=0;i<numUser;i++){
                for (int j=0;j<numMovie;j++){
                    prin.print(ratingMatrix[i][j]+" ");
                }
                prin.println(ratingMatrix[i][numMovie]);
            }
            prin.println("@USERSIM_MATRIX");
            for (int i=0;i<numUser;i++){
                for (int j=0;j<numUser;j++){
                    prin.println(similarity[i][j]+" ");
                }
                prin.println(similarity[i][numUser]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadModel(String modelFilename) {

    }

    @Override
    public double predict(Movie m, User u) {
        double send=0.00;
        double movieN = getAllMovies().keySet().size();
        double userN = getAllUsers().size();
        double targetUser=0;
        double notTarget = userN-targetUser;

        u.getMeanRating();
        for (int i=0;i<userN;i++){
        }
        return send;
    }

    //new method
    public void computeSimilar(){
        double x,y,z;
        int i=0;
        int j=0;
        //find x,y,z
        for (Map.Entry<Integer,User> u:uMap.entrySet()){
            double uMeanRating = u.getValue().getMeanRating();
            for (Map.Entry<Integer,User> v:uMap.entrySet()){
                double vMeanRating = v.getValue().getMeanRating();
                double sim=0;
                double up = 0;
                double down1 = 0;
                double down2 = 0;
                for(Rating r: u.getValue().ratings.values()){
                    Integer movidId = r.m.getID();
                    if(v.getValue().ratings.containsKey(movidId)){
                        y = r.rating - uMeanRating;
                        z = v.getValue().ratings.get(movidId).rating - vMeanRating;
                        up += y*z;
                        down1 += (Math.pow(y, 2));
                        down2 += (Math.pow(z, 2));
                    }
                }
                down1 = Math.sqrt(down1);
                down2 = Math.sqrt(down2);
                if((down1*down2)==0){
                    sim=0;
                    if(u.getKey()==v.getKey()){
                        sim=1;
                    }
                }
                else {
                    sim = up/(down1*down2);
                }
                similarity[i][j]=sim;
                similarity[j][i]=sim;

                j++;
            }
            i++;
            j=0;
        }
    }


    @Override
    public List<MovieItem> recommend(User u, int fromYear, int toYear, int K) {
        List<MovieItem>  back = new ArrayList<>();
        return back;
    }
}
