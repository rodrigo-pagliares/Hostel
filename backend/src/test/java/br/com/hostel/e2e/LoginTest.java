package br.com.hostel.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import br.com.hostel.model.Customer;

@RunWith(JUnitPlatform.class)
public class LoginTest {

	ChromeConnection chromeConnection = new ChromeConnection();
	WebDriver driver = chromeConnection.Connection();
	Customer existentCustomer, nonExistentCustomer;

	@BeforeEach
	public void init() {
		existentCustomer = new Customer("admin@email.com", "123456");
		nonExistentCustomer = new Customer("random@gmail.com", "123456");
		
		driver.get("http://localhost:3000/");
		driver.manage().window().maximize();
	}

	@Test
	public void LoginANonExistentcustomer() throws InterruptedException {

		driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/section/form/input[1]"))
				.sendKeys(nonExistentCustomer.getEmail());
		Thread.sleep(1000);
		driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/section/form/input[2]"))
				.sendKeys(nonExistentCustomer.getPassword());
		Thread.sleep(1000);
		driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/section/form/button")).click();
		Thread.sleep(3000);

		assertEquals(driver.switchTo().alert().getText(), "Falha no Login, tente novamente");
		driver.switchTo().alert().accept();

		Thread.sleep(3000);
		driver.close();
	}

	@Test
	public void RegisterAExistentcustomer() throws InterruptedException {

		driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/section/form/input[1]"))
				.sendKeys(existentCustomer.getEmail());
		Thread.sleep(1000);
		driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/section/form/input[2]"))
				.sendKeys(existentCustomer.getPassword());
		Thread.sleep(1000);
		driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/section/form/button")).click();
		Thread.sleep(3000);

		assertEquals(driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/header/span")).getText(), "Olá Hóspede, bem-vindo ao Hostel!");
		Thread.sleep(3000);

		driver.close();
	}

	public String ConvertLocalDateIntoBrazilianString(LocalDate birthday) {
		return (birthday.getDayOfMonth() < 10 ? (0 + "" + birthday.getDayOfMonth()) : birthday.getDayOfMonth()) + ""
				+ (birthday.getMonth().getValue() < 10 ? (0 + "" + birthday.getMonth().getValue())
						: birthday.getMonth().getValue())
				+ "" + birthday.getYear();
	}

}
