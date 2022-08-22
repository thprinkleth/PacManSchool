public class MaxLifes {

    public static void main(String[] args) {

         int maxLifes = 3;
         int maxLevel = 5;

         for (int level = 0; level <= maxLevel; level++) {
             double topPartM = 1 - maxLifes;
             double bottomPartM = maxLevel;
             double m = topPartM / bottomPartM;
             double first = m * level;
             double last = first + maxLifes;
             last = (int) last;
             System.out.println("Lifes at level " + level + ": " + last);
         }
    }
}
