import java.awt.BasicStroke;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;
import java.util.Random;
import javax.swing.JFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.experimental.chart.renderer.xy.XYSmoothLineAndShapeRenderer;

import com.fazecast.jSerialComm.*;

public class FastVizualization {

	SerialPort comPort;

	String testExample = "744g 743.980896 (16208) adc 200957 raw 599407\r\n"
			+ "743g 743.986694 (16207) adc 200952 raw 599402\r\n" + "743g 743.989929 (16207) adc 200938 raw 599388\r\n"
			+ "744g 744.000122 (16206) adc 200957 raw 599407\r\n" + "743g 743.996216 (16195) adc 200934 raw 599384\r\n"
			+ "743g 743.958008 (16203) adc 200911 raw 599361\r\n" + "743g 743.954102 (16206) adc 200953 raw 599403\r\n"
			+ "743g 743.939514 (16204) adc 200926 raw 599376\r\n" + "744g 743.947571 (16208) adc 200960 raw 599410\r\n"
			+ "743g 743.927917 (16207) adc 200924 raw 599374\r\n" + "743g 743.910828 (16195) adc 200905 raw 599355\r\n"
			+ "743g 743.916382 (16203) adc 200920 raw 599370\r\n" + "743g 743.913818 (16203) adc 200943 raw 599393\r\n"
			+ "743g 743.908508 (16205) adc 200926 raw 599376\r\n" + "743g 743.909668 (16203) adc 200947 raw 599397\r\n"
			+ "743g 743.916565 (16201) adc 200933 raw 599383\r\n" + "744g 743.927246 (16209) adc 200972 raw 599422\r\n"
			+ "744g 743.964233 (16207) adc 200955 raw 599405\r\n" + "749g 744.548523 (16459) adc 202514 raw 600964\r\n"
			+ "724g 741.647522 (16219) adc 195627 raw 594077\r\n" + "743g 741.590393 (16234) adc 200895 raw 599345\r\n";

	BufferedReader getStream(String args[]) throws Exception {
		InputStream in = null;
		if (args.length == 0) {
			in = new StringBufferInputStream(testExample);
			demo = true;
		} else {
			SerialPort comPort = SerialPort.getCommPort(args[0]);
			comPort.setBaudRate(115200);
			comPort.setParity(0);
			comPort.openPort();
			comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 0);//115200
			in = comPort.getInputStream();
		}
		return new BufferedReader(new InputStreamReader(in, "utf8"));
	}

	void printPorts() {
		SerialPort ports[] = SerialPort.getCommPorts();
		for (SerialPort p : ports) {
			System.out.println(p.getSystemPortName());
		}
	}

	double getValue(String line) throws Exception {
		try {
		  int gi = line.indexOf("g");
		  return Double.parseDouble(line.substring(gi+1, line.indexOf(' ',gi+2)).trim());
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	void processStream(BufferedReader br) throws Exception {
		String line = null;
		while ((line = br.readLine()) != null) {
			System.out.println(line);
			double val = getValue(line);
		}
	}

	BufferedReader br;

	boolean demo = false;

	public static void main(String args[]) throws Exception {
		FastVizualization fv = new FastVizualization();
		fv.printPorts();
		fv.br = fv.getStream(args);
		fv.graphGo();
		if (fv.comPort != null) {
			fv.comPort.closePort();
		}
	}

	static TimeSeries ts = new TimeSeries(" ", Millisecond.class);

	void graphGo() {
		
		gen myGen = new gen();
		new Thread(myGen).start();

		TimeSeriesCollection dataset = new TimeSeriesCollection(ts);
		JFreeChart chart = ChartFactory.createTimeSeriesChart(" ", "Time", "Weight(g)", dataset, true, true, false);
		final XYPlot plot = chart.getXYPlot();
		
		ValueAxis axis = plot.getDomainAxis();
		axis.setAutoRange(true);
		axis.setFixedAutoRange(60000.0);

		ValueAxis raxis = plot.getRangeAxis();
		raxis.setRange(-100, 50);
		
		XYSmoothLineAndShapeRenderer renderer = new XYSmoothLineAndShapeRenderer();
		//renderer.setBaseStroke(new BasicStroke(5));
		//renderer.setUseFillPaint(false);
		//renderer.setBaseStroke(new BasicStroke(5), true);
		renderer.setSeriesStroke(0, new BasicStroke(7.0f));
        plot.setRenderer(renderer);
		
		JFrame frame = new JFrame(" ");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ChartPanel label = new ChartPanel(chart);
		frame.getContentPane().add(label);
		// Suppose I add combo boxes and buttons here later

		frame.pack();
		frame.setVisible(true);
	}

	class gen implements Runnable {
		private Random randGen = new Random();

		public void run() {
			try {
				String line = null;
				while ((line = br.readLine()) != null) {					
					double val = getValue(line);
					System.out.println(line+" graph value "+val);
					//val-=1000;
					//if(val<0) val=0;
					ts.addOrUpdate(new Millisecond(), val);
					if (demo) {
						try {
							Thread.sleep(300);
						} catch (InterruptedException ex) {
							System.out.println(ex);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
