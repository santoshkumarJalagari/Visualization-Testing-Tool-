package packageName;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;


public class VisualizationTool {
	
	static WebDriver driver=null;
	static String screenShotType="driver";
	static String fileextenstion="jpg";
	static String expectedImagesRepositoryPath="";
	static String actualImagesRepositoryPath="";
	static String resultImagesRepositoryPath="";
	static String scriptName="Not Provided";
	static String browser="Not provided";
	static String reportHeader="Visualizationt Report";
//	static String reportFileName="";
	static String reportFilePath="C:"+File.separator+"visualization";
	static private int failCount=0;
	
	public VisualizationTool(WebDriver dirver,
			String expectedImagesRepositoryPath,
			String actualImagesRepositoryPath,
			String resultImagesRepositoryPath){
		this.driver=dirver;
		this.expectedImagesRepositoryPath=expectedImagesRepositoryPath;
		this.actualImagesRepositoryPath=actualImagesRepositoryPath;
		this.resultImagesRepositoryPath=resultImagesRepositoryPath;
		reportFilePath=reportFilePath+File.separator+getUniqueReportName()+".html";
//		deleteFileReport();
		writeHTMLHeader();
		writeDetailBanner(scriptName, getBrowserDetails(), System.getProperty("os.name").toLowerCase());
		
	}
	
	private String getBrowserDetails(){
		Capabilities cap = ((RemoteWebDriver) driver).getCapabilities();
		String browserName = cap.getBrowserName().toLowerCase();
		String browserVersion = cap.getVersion();
		return " " +browserName +"(" +browserVersion+")";
	}
	private String getUniqueReportName(){
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
		Date date = new Date(); 
		String now=dateFormat.format(date);
		return "visualzationReport"+now;
	}
	public void validatePage(String pageName){
		String expectedImagePath=expectedImagesRepositoryPath+File.separator+getImageName(pageName);
		String actualImagePath=actualImagesRepositoryPath+File.separator+getImageName(pageName);
		String resultImagePath=resultImagesRepositoryPath+File.separator+getImageName(pageName);
		if(checkIfExpectedImageExist(pageName)){
			deleteFile(actualImagesRepositoryPath);
			deleteFile(resultImagesRepositoryPath);
			takeScreenShot(screenShotType, fileextenstion, actualImagePath);
			try {
				String passOrFail=similarity(expectedImagePath, actualImagePath,resultImagePath);
				recordStep(pageName, passOrFail, expectedImagePath, actualImagePath,resultImagePath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	private String getImageName(String pageName){
		return pageName+"_Img."+fileextenstion;
	}
	private boolean  checkIfExpectedImageExist(String pageName){
		boolean isImageExist=true;
		String filePath=expectedImagesRepositoryPath+File.separator+getImageName(pageName);
		File existingFile=new File(filePath);
		if(!existingFile.exists()){
			isImageExist=false;
			takeScreenShot(screenShotType, fileextenstion, filePath);
			System.out.println("#################################################Page Visualization#################################################");
			System.out.println("Image for "+pageName+" page does not exist, we have take create image at path :"+filePath);
			System.out.println("Every time this image will be compared againest "+pageName+" page,so please make sure the image is fine and perfect to use");
			System.out.println("####################################################################################################################");
		}
		return isImageExist;
	}
	
	private void takeScreenShot(
			String screenShotType,
			String fileextenstion,
			String filePath){
		
		if(screenShotType.equalsIgnoreCase("Robot")){
		        Robot robot;
				try {
					Thread.sleep(1000 * 2);
					robot = new Robot();
					robot.keyPress(KeyEvent.VK_ALT);
			        robot.keyPress(KeyEvent.VK_PRINTSCREEN);
			        robot.keyRelease(KeyEvent.VK_PRINTSCREEN);
			        robot.keyRelease(KeyEvent.VK_ALT);
			        Thread.sleep(1000 * 2);
			        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
			        RenderedImage image = (RenderedImage) t.getTransferData(DataFlavor.imageFlavor);
			        boolean isSuccess = ImageIO.write(image, "png", new File(filePath));
				} catch (AWTException e1) {
					e1.printStackTrace();
				}catch (InterruptedException e) {
		            throw new RuntimeException(e);
		        } catch (UnsupportedFlavorException e) {
		        	System.out.println("[Info]-->No active screen, so ignoring screenshot capture");
				} catch (IOException e) {
					e.printStackTrace();
				}
				
		}else{
			File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
			// Now you can do whatever you need to do with it, for example copy somewhere
			try {
				FileUtils.copyFile(scrFile, new File(filePath));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private String similarity( String expectedImagePath,
			String  actualImagepath,
			String resultFilePath) throws IOException{
		
        int total_no_ofPixels = 0;      
        int image1_PixelColor, red, blue, green;
        int image2_PixelColor, red2, blue2, green2;
        float differenceRed, differenceGreen, differenceBlue, differenceForThisPixel;
        double nonSimilarPixels = 0l, non_Similarity = 0l;
        String passORFail="fail";
        BufferedImage image1= null;
		BufferedImage image2 = null;
		try {
			image1 = ImageIO.read(new File(expectedImagePath));
			image2=	 ImageIO.read(new File(actualImagepath));
		}catch(IOException e){
			System.out.println("Error at buffer image");
		}
//        image1=image1.getSubimage(1,25,image1.getWidth()-50, image1.getHeight()-75);
//        image2=image2.getSubimage(1,25,image2.getWidth()-50, image2.getHeight()-75);
        Graphics2D gc = image2.createGraphics();
        Color color = new Color(1, 0, 0, 0.08f); //Red 
        gc.setColor(color);
        long startTime = System.nanoTime();
//A digital image is a rectangular grid of pixels, Dimensions with/Height = 1366/728 pixels.        
//Colors are usually expressed in terms of a combination of red, green and blue values.        
        for (int row = 0; row < image1.getWidth(); row++) {
           for (int column = 0; column < image1.getHeight(); column++) {
                 image1_PixelColor   =  image1.getRGB(row, column);   
//                 System.out.println("Image 1: "+image1_PixelColor);
                 red                 = (image1_PixelColor & 0x00ff0000) >> 35;
                 green               = (image1_PixelColor & 0x0000ff00) >> 12;
                 blue                =  image1_PixelColor & 0x000000ff;

                 image2_PixelColor   =  image2.getRGB(row, column);    
//                 System.out.println("Image 2: "+image2_PixelColor);
                 red2                = (image2_PixelColor & 0x00ff0000) >> 35;
                 green2              = (image2_PixelColor & 0x0000ff00) >> 12;
                 blue2               =  image2_PixelColor & 0x000000ff;

                       if (red != red2 || green != green2 || blue != blue2) {
                           differenceRed   =  red - red2 / 255;
                           differenceGreen = ( green - green2 ) / 255;
                           differenceBlue  = ( blue - blue2 ) / 255;
                           differenceForThisPixel = ( differenceRed + differenceGreen + differenceBlue ) / 3;
                           nonSimilarPixels += differenceForThisPixel;
                           gc.drawRect(row-1, column-1,1, 1);
                       }
                total_no_ofPixels++;

                if ( image1_PixelColor != image2_PixelColor ) {
//                	gc.drawRect(row-1, column-1,1, 1);
//                    image2.setRGB(row, column, Color.red.getRed());
                    
                }
           }
       }
        long endTime = System.nanoTime();
        System.out.println(String.format( "%-2d: %s", 0, toString(endTime - startTime )));

        System.out.println(" Writing the difference of first_Image to Second_Image ");
        ImageIO.write(image2, "jpeg", new File(resultFilePath));

        non_Similarity = (nonSimilarPixels / total_no_ofPixels);
        System.out.println( "Total No of pixels : " + total_no_ofPixels +"\t Non Similarity is : " + non_Similarity +"%");
        if(non_Similarity>20){
        	failCount++;
        }else{
        	passORFail="pass";
        }
        return passORFail;          
    }
	private String toString(long nanoSecs) {
         int minutes    = (int) ( nanoSecs / 60000000000.0 );
         int seconds    = (int) ( nanoSecs / 1000000000.0 )  - ( minutes * 60 );
         int millisecs  = (int) ( (( nanoSecs / 1000000000.0 ) - ( seconds + minutes * 60 )) * 1000 );

         if      ( minutes == 0 && seconds == 0   )    return millisecs + "ms";
         else if ( minutes == 0 && millisecs == 0 )    return seconds + "s";
         else if ( seconds == 0 && millisecs == 0 )    return minutes + "min";
         else if ( minutes == 0                   )    return seconds + "s " + millisecs + "ms";
         else if ( seconds == 0                   )    return minutes + "min " + millisecs + "ms";
         else if ( millisecs == 0                 )    return minutes + "min " + seconds + "s";

         return minutes + "min " + seconds + "s " + millisecs + "ms";
      }
    private  Date cvtToGmt( Date date ){
		TimeZone tz = TimeZone.getDefault();
		Date ret = new Date( date.getTime() - tz.getRawOffset() );

		// if we are now in DST, back off by the delta.  Note that we are checking the GMT date, this is the KEY.
		if ( tz.inDaylightTime( ret )){
			Date dstDate = new Date( ret.getTime() - tz.getDSTSavings() );

			// check to make sure we have not crossed back into standard time
			// this happens when we are on the cusp of DST (7pm the day before the change for PDT)
			if ( tz.inDaylightTime( dstDate )){
				ret = dstDate;
			}
		}
		return ret;
	}
    
    
    
    private void writeHTMLHeader(){
    	File reportFile=new File(reportFilePath);
		FileWriter fw;
		try {
			fw = new FileWriter(reportFile, true);
		
			 PrintWriter file = new PrintWriter(fw);
			 //Starting body of the html
			 file.println("<html>");
			 file.println("	<head>");
			 file.println("		<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
			 file.println("		<title>Visualization Report</title>");
			 file.println("		<style type=\"text/css\">");
			 		
			 file.println("		body{");
			 file.println("			margin: 20px;");
			 file.println("			padding: 0;");
			 file.println("			overflow: hidden;");
			 file.println("			height: 100%; ");
			 file.println("			max-height: 100%; ");
			 file.println("			font-family:Sans-serif;");
			 file.println("			line-height: 1.5em;");
			 file.println("		}");
			 		
			 file.println("		nav{");
			 file.println("			position: fixed;");
			 file.println("			top: 85px;");
			 file.println("			bottom: 0; ");
			 file.println("			left: 0;");
			 file.println("			width: 300px; /* Width of navigation frame */");
			 file.println("			overflow: auto; /* Disables scrollbars on the navigation frame. To enable scrollbars, change \"hidden\" to \"scroll\" */");
			 file.println("			background: #eee;");
			 file.println("			border: 2px solid gray");
			 file.println("		}");
			 		
			 file.println("		main{");
			 file.println("			position: fixed;");
			 file.println("			top: 85px; ");
			 file.println("			left: 300px; /* Set this to the width of the navigation frame */");
			 file.println("			right: 0;");
			 file.println("			bottom: 0;");
			 file.println("			overflow: auto; ");
			 file.println("			background: #fff;");
			 file.println("			border: 2px solid gray");
			 file.println("		}");
			 		
			 file.println("		banner{");
			 file.println("			position: fixed;");
			 			
			 file.println("			top: 0; ");
			 file.println("			left: 0; /* Set this to the width of the navigation frame */");
			 file.println("			right: 0;");
			 file.println("			/*bottom: 50px;*/");
			 file.println("			overflow: auto; ");
			 file.println("			background: #3498DB;");
			 			
			 file.println("		}");
			 file.println("		detailBanner{");
			 file.println("			position: fixed;");
			 file.println("			top: 60px; ");
			 file.println("			height: 25px;");
			 file.println("			left: 0; /* Set this to the width of the navigation frame */");
			 file.println("			right: 0;");
			 file.println("			overflow: hidden; ");
			 file.println("			background: #F0EFFF;");
			 file.println("		}");
			 file.println("		.innertube{");
			 file.println("			margin-left: 15px; /* Provides padding for the content */");
			 			
			 file.println("		}");
			 		
			 file.println("		p {");
			 file.println("			color: #555;");
			 file.println("		}");
			 file.println("		.leftSideData{");
			 file.println("			bottom: 0; ");
			 file.println("			left: 0;");
			 file.println("			/*width: 280px;*/ /* Width of navigation frame */");
			 file.println("			background: #eee;");
			 file.println("		}");
			 file.println("		nav ul {");
			 file.println("			list-style-type: none;");
			 file.println("			margin: 0;");
			 file.println("			padding: 0;");
			 file.println("		}");
			 file.println("		.banner{");
			 file.println("			margin-top: 15px");
			 file.println("			margin-left: 10px;");
			 file.println("		    margin-right: 10px;");
			 file.println("		    text-align: center;");
			 file.println("			vertical-align: center;");
			 file.println("		}");
			 file.println("		.pageInfo{");
			 file.println("			margin-top: 5px");
			 file.println("			margin-left: 2px;");
			 file.println("		    margin-right: 2px;");
			 file.println("		    text-align: center;");
			 file.println("			vertical-align: center;");
			 file.println("		}");
			 file.println("		.detailbanner{");
			 			
			 			
			 file.println("		    margin-right: 5px;");
			 file.println("			text-align:center;");

			 			
			 file.println("		}");
			 file.println("		.verticalLineInDetailBanner{");
			 file.println("			margin-top:1px;");
			 file.println("			margin-bottom:1px;");
			 file.println("			background-color:blue;");
			 file.println("		}");
			 file.println("		h5{");
			 file.println("			background: #3498DB;");
			 file.println("			margin: 0;");
			 file.println("			margin-top:20px;");
			 file.println("			margin-bottom:2px;");
			 file.println("			margin-right: 20px;");
			 file.println("			color:whitesmoke;");
			 file.println("			text-align:center;");
			 file.println("		}");
			 file.println("		h6{");
			 file.println("			text-align:center;");
			 file.println("			display:inline;");
			 file.println("			margin-right: 100px;");
			 file.println("			background-color: #CECED6;");
			 file.println("			border: 3px solid #CECED6;");
			 file.println("			border-radius:12px 10px 10px 10px;");
			 			
			 file.println("		}");
			 file.println("		nav ul a {");
			 file.println("			color: darkgreen;");
			 file.println("			text-decoration: none;");
			 file.println("		}");
			 file.println("		li{");
			 file.println("			font-size: 90%;");
			 file.println("			margin-right: 20px;");
			 file.println("			margin-bottom:2px;");
			 file.println("			background: #D7D7D7;");
			 file.println("			text-align:center;");
			 			
			 			
			 file.println("		}");
			 		
			 				
			 file.println("		/*IE6 fix*/");
			 file.println("		* html body{");
			 file.println("			padding: 0 0 0 230px; /* Set the last value to the width of the navigation frame */");
			 file.println("		}");
			 		
			 file.println("		* html main{ ");
			 file.println("			height: 100%; ");
			 file.println("			width: 100%; ");
			 file.println("		}");
			 		
			 file.println("		</style>");
			 		
			 file.println("		<script type=\"text/javascript\">");
			 file.println("			/* =============================");
			 file.println("			This script generates sample text for the body content. ");
			 file.println("			You can remove this script and any reference to it. ");
			 file.println("			 ============================= */");
			 		
			 file.println("			function updateTextInMain(imagePathToShow){");
			 file.println("				document.getElementById(\"showImage\").src =imagePathToShow;		");		
			 file.println("				return false;");
			 file.println("			}");
			 file.println("		</script>	");
			 	
			 file.println("	</head>");
			 file.println("	<body>");
				
			 file.println("			<banner>");
			 file.println("				<div class=\"banner\">");
			 file.println("					<h1 style=\"color:whitesmoke\">"+reportHeader+"</h1>");
			 file.println("				</div>");
						
			 file.println("			</banner>");
			 file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
    }
    
    private void writeDetailBanner(String scriptName,
    		String browser,
    		String operatingSystem
//    		String failedCount,
//    		String totalCount
    		){
    	File reportFile=new File(reportFilePath);
		FileWriter fw;
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date(); 
		String now=dateFormat.format(date);
		try {
			fw = new FileWriter(reportFile, true);
		
			 PrintWriter file = new PrintWriter(fw);
			 //Starting body of the html
			 file.println("	<detailBanner>");
			 file.println("				<div class=\"detailbanner\">");
			 file.println("					<h6><u>Script Name</u>: "+scriptName+"");
			 file.println("					<h6><u>Browser</u>: "+browser+"</h6>");
			 file.println("					<h6><u>OS</u>: "+operatingSystem+"</h6>");
			 file.println("					<h6><u>Date & Time</u> : "+now+" </h6>");
//			 file.println("					<h6><u>Failed</u> : "+failedCount+"/"+totalCount+" </h6>");
			 file.println("				</div>");
			 file.println("				<div class=\"verticalLineInDetailBanner\">");
			 file.println("				<p></p>");
			 file.println("				</div>");
			 file.println("			</detailBanner>");
			 file.println("<main>");
			 file.println("			<div class=\"innertube\">");
			 file.println("				<img style='height: 96%; width: 100%; object-fit: contain; margin-top:10px; ' id =\"showImage\" src=''>");
			 file.println("			</div>");
			 file.println("		</main>	");
			 file.println("<nav id=\"nav\">");
			 file.println("			<div class=\"innertube\">");
			 file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
    }
    private void recordStep(String pageName,
    		String status,
    		String expectedScreenShotpath,
    		String actualScreenShotpath,
    		String resultImagePath
    		){
    	
    	String colorCode="";
    	if (status.equalsIgnoreCase("pass")){
    		colorCode="green";
    	}else{
    		colorCode="red";
    	}
    	File reportFile=new File(reportFilePath);
		FileWriter fw;
		try {
			fw = new FileWriter(reportFile, true);
		
			 PrintWriter file = new PrintWriter(fw);
			 file.println("<h5>"+pageName+"</h5>");
			 file.println("			<ul class='leftSideData'>");
			 file.println("				<li><a style=\"color:black;\" onclick=updateTextInMain(\""+expectedScreenShotpath.replace("\\", "/")+"\") href=\"#\" >Expected</a></li>");
			 file.println("				<li><a style=\"color:black;\"  onclick=updateTextInMain(\""+actualScreenShotpath.replace("\\", "/")+"\") href=\"#\">Actual</a></li>");
			 file.println("				<li><a style=\"color:"+colorCode+";\" onclick=updateTextInMain(\""+resultImagePath.replace("\\", "/")+"\") href=\"#\">Difference</a></li>");
			 file.println("			</ul>");
			 file.println("         <p></p>");
			 file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
    }
    private void deleteFile(String filepath){
		File file =new File(filepath);
		file.deleteOnExit();
	}
    private void deleteFileReport(){
		File file =new File(reportFilePath);
		file.deleteOnExit();
	}
    private void closeHTML(){
    	File reportFile=new File(reportFilePath);
		FileWriter fw;
		try {
			fw = new FileWriter(reportFile, true);
		
			 PrintWriter file = new PrintWriter(fw);
			 file.println("</div>");
			 file.println("			<p></p>");
			 file.println("		</nav>");
			 file.println("	</body>");
			 file.println("</html>");
			 file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
    }
//    private String getBrowserName(){
//		Capabilities caps = ((RemoteWebDriver) driver).getCapabilities();
//		System.out.println(caps.getBrowserName());
//		System.out.println(caps.getVersion());
//	}
//	public static void main(String[] args) throws IOException {
//		String temp="Discover 0035"+"\n "+"Santosh";
//		System.out.println(temp.split("\\n")[0]);
		
		
		
//		HtmlReport report= new HtmlReport("firefox", "STG", "TestingImage");
//		Util.sleep(10000);
//		String fileName=report.captureScreeShot();
//		String filePath="C:\\SeleniumTestResults\\"+fileName;
//		Util.sleep(10000);
//		String fileName2=report.captureScreeShot();
//		String filePath2="C:\\SeleniumTestResults\\"+fileName2;
//		 int red                 = (50 & 100) ;
//		System.out.println(red);
//		String filePath="C:\\image1.jpg";
//		String filePath2="C:\\image2.jpg";
//		
//		BufferedImage image1=ImageIO.read(new File(filePath));
//		BufferedImage image2=ImageIO.read(new File(filePath2));
//		
//		similarity(image1, image2);
		
//	}
}
