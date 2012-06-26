package kokkodis.utils;




public class TrapezoidRule {

	


	public static double calculate (double[] x, double[] y)
   { 
		double sum = 0.0,
             increment;

      for ( int k = 1; k < x.length; k++ )
      {//Trapezoid rule:  1/2 h * (f0 + f1)
         increment = 0.5 * (x[k]-x[k-1]) * (y[k]+y[k-1]);
         sum += increment;
        //System.out.println(x[k]+" "+ x[k-1] + " " + y[k] + " " + y[k-1]+ "sum:"+sum);
      }
      return sum;
   }

  
}
