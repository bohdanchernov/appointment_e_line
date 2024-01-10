package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class FirefoxClientRunner {
    @Autowired
    private RestTemplate restTemplate;
    //Spanish VPN is required
    private static final String START_URL = "https://icp.administracionelectronica.gob.es/icpco/icpplus";

    //todo make vars dynamic?
    @Value("${app.observer.web.driver.path}")
    private String WEB_DRIVER_PATH;
    @Value("${app.observer.user.nie}")
    private String NIE;
    @Value("${app.observer.user.name}")
    private String NAME_LAST_NAME;

    private boolean isFirstRun = true;

    @Scheduled(fixedDelay = 600000, initialDelay = 10000)
    public void runFirefoxAndCheckAppointment() {
        if (isFirstRun) {
            System.out.println("WEB_DRIVER_PATH: " + WEB_DRIVER_PATH);
            System.out.println("NIE: " + NIE);
            System.out.println("NAME_LAST_NAME: " + NAME_LAST_NAME);
            isFirstRun = false;
        }
        if (WEB_DRIVER_PATH == null || WEB_DRIVER_PATH.equals("web.driver.path")) {
            throw new RuntimeException("WebDriver path should be specified");
        }
        if (NIE == null) {
            throw new RuntimeException("NIE should be specified");
        }
        if (NAME_LAST_NAME == null) {
            throw new RuntimeException("Name and last name should be specified");
        }

        boolean notFound = true;
        System.setProperty("webdriver.chrome.driver", WEB_DRIVER_PATH);
        FirefoxOptions options = new FirefoxOptions();
        WebDriver driver = new FirefoxDriver(options);
        try {
            driver.manage().window().maximize();
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Thread.sleep(5000);

            driver.navigate().to(START_URL);

            Thread.sleep(20000);


            List<WebElement> regionSelects = driver.findElements(By.name("form"));
            if (regionSelects == null || regionSelects.isEmpty()) {
                driver.close();
                throw new RuntimeException("Page wasn't initialized yet");
            }


            Select regionSelect = new Select(regionSelects.get(0));


            regionSelect.selectByVisibleText("Alicante");
            Thread.sleep(5000);
            WebElement regionSubmitButton = driver.findElement(By.id("btnAceptar"));
            regionSubmitButton.click();


            Thread.sleep(5000);

            List<WebElement> officeSelectWebElements = driver.findElements(By.id("sede"));
            if (officeSelectWebElements == null || officeSelectWebElements.isEmpty()) {

                Thread.sleep(5000);

                WebElement bodyTag = driver.findElement(By.tagName("body"));
                String bodyText = bodyTag.getText();
                if (bodyText.contains("The requested URL was rejected. Please consult with your administrador.")) {
                    driver.close();
                    throw new RuntimeException("Often requests. Ip is blocked temp, try after 10 minutes");
                }
                throw new RuntimeException("Can't find office selection");
            }

            Select officeSelect = new Select(officeSelectWebElements.get(0));
            officeSelect.selectByVisibleText("CNP Alicante TIE, Campo de Mirra, 6");
            Thread.sleep(5000);
            Select visitReasonSelect = new Select(driver.findElement(By.id("tramiteGrupo[0]")));
            visitReasonSelect.selectByVisibleText("POLICÍA TARJETA CONFLICTO UCRANIA–ПОЛІЦІЯ -КАРТКА ДЛЯ ПЕРЕМІЩЕНИХ ОСІБ ВНАСЛІДОК КОНФЛІКТУ В УКРАЇНІ");
            WebElement officeAndReasonSubmitButton = driver.findElement(By.id("btnAceptar"));
            officeAndReasonSubmitButton.click();

            Thread.sleep(5000);
            js.executeScript("window.scrollBy(0,1500)", "");
            WebElement formSubmitButton = driver.findElement(By.id("btnEntrar"));
            formSubmitButton.click();

            Thread.sleep(5000);
            WebElement nieInput = driver.findElement(By.id("txtIdCitado"));
            nieInput.sendKeys(NIE);
            Thread.sleep(5000);
            WebElement nameLastNameInput = driver.findElement(By.id("txtDesCitado"));
            nameLastNameInput.sendKeys(NAME_LAST_NAME);
            WebElement nieNameLastNameSubmitButton = driver.findElement(By.id("btnEnviar"));
            nieNameLastNameSubmitButton.click();

            Thread.sleep(5000);
            WebElement getAppointmentButton = driver.findElement(By.id("btnEnviar"));
            getAppointmentButton.click();

            Thread.sleep(5000);
            try {
                WebElement appointmentResultMessageInfo = driver.findElement(By.className("mf-msg__info"));

                List<WebElement> message = appointmentResultMessageInfo.findElements(By.tagName("span"));

                if (message != null && !message.isEmpty() && message.get(0).getText().contains("En este momento no hay citas disponibles")) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    String formattedDateTime = LocalDateTime.now().format(formatter); // "1986-04-08 12:30"

                    System.out.println(formattedDateTime + ". " + "Nothing found");
                    Thread.sleep(5000);
                    notFound = false;
                } else {
                    restTemplate.getForEntity("http://localhost:8081/nieFound/" + NIE, String.class);
                }
            } catch (Exception e) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                String formattedDateTime = LocalDateTime.now().format(formatter); // "1986-04-08 12:30"
                System.out.println(formattedDateTime + ". " + "Appointment might be available");
                System.out.println(e.getMessage());
                restTemplate.getForEntity("http://localhost:8081/nieFound/" + NIE, String.class);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (!notFound) {
                driver.close();
            }
        }

    }
}
