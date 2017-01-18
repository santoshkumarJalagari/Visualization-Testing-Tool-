
package packageName;


import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TestName;
import org.xml.sax.SAXException;


import common.Util;
import common.exception.MetadataException;

public class Test_Verify_Demo_VisualizationTool extends BornInCloudTestBase {
	
	BornInCloud bic = null;
	String contractNumber="";
	String expectedPrice="";
	String expectedDate="";
	String expectedPriceOrderHistory="";
	String expectedPriceSymbol="";
	String expectedDateOrderHistory="";
	String expectedImagesRepositoryPath="C:\\visualization\\expected";
	String actualImagesRepositoryPath="C:\\visualization\\actual";
	String resultImagesRepositoryPath="C:\\visualization\\difference";
	TestName tname=null;
	static VisualizationTool visu=null;
	public Test_Verify_Demo_VisualizationTool() throws ParserConfigurationException, SAXException, IOException {
		super("Browser",getAppBrowser());	
		tname = new TestName();
	}

	@Before
	public void launchBrowser() {
		launchIPP(getPortalURL());
		visu=new VisualizationTool(EISTestBase.driver, expectedImagesRepositoryPath, actualImagesRepositoryPath, resultImagesRepositoryPath);
		visu.scriptName=tname.toString();
		visu.browser="Chrome";
		visu.validatePage("Check_loginPage");
	}

	@Test
	public void Validate_() throws Exception{
		loginToPortal();
		 visualValidation("Magyar","Magyar");
         visualValidation("Deutsch","Deutsch");
         visualValidation("Italiano","Italiano");
         visualValidation("Español","Espanol");
	}
	
	 public static void visualValidation(String languageValue, String lanuageName){
    	 changeLanguage(languageValue);
         
         // Visual validation point #1 Eg: Magyar
    	 visu.validatePage("Check_"+lanuageName);
    }
	public static void changeLanguage(String language){
		homePage.waitForFieldVisible("profileLinkInPortal", 30000);
		
    	 //Click on Profile  
		homePage.click("profileLinkInPortal");
		
		homePage.waitForFieldVisible("preferencesUnderProfile", 10000);
        
        //Click on preferences  
        homePage.click("preferencesUnderProfile");
        
        
        homePage.waitForFieldVisible("languageButtonUnderPreferences", 10000);
        
        //Click on Change Language  
        homePage.click("languageButtonUnderPreferences");
        
        
        homePage.waitForFieldVisible("languageListUnderPreferences", 5000);
        
        //select Language
        homePage.populateField("languageListUnderPreferences", language);
      
        //Click on Save Language  
        homePage.click("saveLanguageButtonInPreferences");
        Util.sleep(5000);
        //Click on Management page  
        homePage.click("managementLinkInPortal");
        Util.sleep(20000);
        homePage.waitForFieldAbsent("cloudCreditImageInfor", 25000);
        homePage.waitForFieldVisible("productsPageINPortal", 30000);
       
    }
	
	
	
	public void loginToPortal() throws Exception{
		bic = utilCreateMyAutodeskPortal();
		loginPage = bic.getLoginPage();
		homePage = bic.getHomePage();
		String env;

		env = getEnvironment();
		if (env.equalsIgnoreCase("DEV")) {
			bic.login(testProperties.getConstant("USER_ID_DEV"),testProperties.getConstant("PASSWORD_DEV"));
		} else if (env.equalsIgnoreCase("STG")) {
			bic.login(testProperties.getConstant("USER_ID_STG"),testProperties.getConstant("PASSWORD_STG"));
		}
		homePage.waitForElementToDisappear("pageLoadImg", 100000);
				
		homePage.waitForFieldVisible("titleproductservices", 30000);

		boolean text = homePage.getValueFromGUI("titleproductservices").contains("PRODUCTS & SERVICES");
		
		if (getEnvironment().equalsIgnoreCase("DEV")){
			String url=getPortalURL()+"/cep/"+testProperties.getConstant("mru_"+getEnvironment().toUpperCase());
			driver.navigate().to(url);
		}
//		EISTestBase.assertTrue("Customer Portal Home Page is Displayed", text);
		Util.sleep(20000);
		homePage.waitForFieldVisible("profileLinkInPortal", 120000);
		
		
	}
	
	public void logoutPortal(){
		// Logout from Portal
				homePage.click("arrow");
				homePage.click("signout");
	}
	@After
	public void tearDown() throws Exception {

		// Close the browser. Call stop on the WebDriverBackedSelenium instance
		// instead of calling driver.quit(). Otherwise, the JVM will continue
		// running after the browser has been closed.
		driver.quit();

		// TODO Figure out how to determine if the test code has failed in a
		// manner other than by EISTestBase.fail() being called. Otherwise,
		// finish() will always print the default passed message to the console.
		finish();
	}
}









