package com.gxt.aicodegenerationplatform.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.gxt.aicodegenerationplatform.exception.BusinessException;
import com.gxt.aicodegenerationplatform.exception.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.UUID;


/**
 * 截图工具类
 */
@Slf4j
public class WebScreenshotUtils {

    private static volatile WebDriver webDriver = null;

    // 使用相对路径指向 resources 目录下的 ChromeDriver
    private static final String CHROME_DRIVER_PATH =
            System.getProperty("user.dir") + "/src/main/resources/web_drivers/chromedriver.exe";
    private static final int DEFAULT_WIDTH = 1600;
    private static final int DEFAULT_HEIGHT = 900;

    /**
     * 退出时销毁
     */
    @PreDestroy
    public static void destroy() {
        if (webDriver != null) {
            try {
                webDriver.quit();
            } catch (Exception e) {
                log.warn("关闭 WebDriver 时出现异常", e);
            } finally {
                webDriver = null;
            }
        }
    }

    /**
     * 生成网页截图
     *
     * @param webUrl 要截图的网址
     * @return 压缩后的截图文件路径，失败返回 null
     */
    public static String saveWebPageScreenshot(String webUrl) {
        // 非空校验
        if (StrUtil.isBlank(webUrl)) {
            log.error("网页截图失败，url为空");
            return null;
        }

        WebDriver driver = null;
        try {
            // 每次调用创建新的 WebDriver 实例
            driver = getWebDriver();

            // 创建临时目录
            String rootPath = System.getProperty("user.dir") + "/tmp/screenshots/" + UUID.randomUUID().toString().substring(0, 8);
            FileUtil.mkdir(rootPath);

            // 图片后缀
            final String IMAGE_SUFFIX = ".png";
            // 原始图片保存路径
            String imageSavePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + IMAGE_SUFFIX;

            // 访问网页
            driver.get(webUrl);

            // 等待网页加载
            waitForPageLoad(driver);

            // 截图
            byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

            // 保存原始图片
            saveImage(screenshotBytes, imageSavePath);
            log.info("原始截图保存成功：{}", imageSavePath);

            // 压缩图片
            final String COMPRESS_SUFFIX = "_compressed.jpg";
            String compressedImagePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + COMPRESS_SUFFIX;
            compressImage(imageSavePath, compressedImagePath);
            log.info("压缩图片保存成功：{}", compressedImagePath);

            // 删除原始图片
            FileUtil.del(imageSavePath);
            return compressedImagePath;
        } catch (Exception e) {
            log.error("网页截图失败：{}", webUrl, e);
            return null;
        } finally {
            // 关闭当前 WebDriver 实例
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception e) {
                    log.warn("关闭 WebDriver 时出现异常", e);
                }
            }
        }
    }

    /**
     * 获取 WebDriver 实例
     * @return WebDriver 实例
     */
    private static WebDriver getWebDriver() {
        try {
            // 检查 ChromeDriver 文件是否存在
            File driverFile = new File(CHROME_DRIVER_PATH);
            if (!driverFile.exists()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                        "ChromeDriver 未找到，请确保文件存在于: " + CHROME_DRIVER_PATH);
            }

            // 设置本地 ChromeDriver 路径
            System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_PATH);

            // 配置 Chrome 选项
            ChromeOptions options = new ChromeOptions();
            // 无头模式
            options.addArguments("--headless");
            // 禁用GPU（在某些环境下避免问题）
            options.addArguments("--disable-gpu");
            // 禁用沙盒模式（Docker环境需要）
            options.addArguments("--no-sandbox");
            // 禁用开发者shm使用
            options.addArguments("--disable-dev-shm-usage");
            // 设置窗口大小
            options.addArguments(String.format("--window-size=%d,%d", DEFAULT_WIDTH, DEFAULT_HEIGHT));
            // 禁用扩展
            options.addArguments("--disable-extensions");
            // 设置用户代理
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

            // 创建驱动
            WebDriver driver = new ChromeDriver(options);

            // 设置页面加载超时
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            // 设置隐式等待
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

            return driver;
        } catch (Exception e) {
            log.error("初始化 Chrome 浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 Chrome 浏览器失败: " + e.getMessage());
        }
    }

    /**
     * 保存图片到文件
     *
     * @param imageBytes 图片字节数据
     * @param imagePath 保存路径
     */
    private static void saveImage(byte[] imageBytes, String imagePath) {
        try {
            FileUtil.writeBytes(imageBytes, imagePath);
        } catch (Exception e) {
            log.error("保存图片失败：{}", imagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
        }
    }

    /**
     * 压缩图片
     *
     * @param originImagePath 原始图片路径
     * @param compressedImagePath 压缩后图片路径
     */
    private static void compressImage(String originImagePath, String compressedImagePath) {
        // 压缩图片质量（0.1 = 10% 质量）
        final float COMPRESSION_QUALITY = 0.3f;
        try {
            ImgUtil.compress(
                    FileUtil.file(originImagePath),
                    FileUtil.file(compressedImagePath),
                    COMPRESSION_QUALITY
            );
        } catch (Exception e) {
            log.error("压缩图片失败：{} -> {}", originImagePath, compressedImagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩图片失败");
        }
    }

    /**
     * 等待页面加载完成
     *
     * @param webDriver WebDriver 实例
     */
    private static void waitForPageLoad(WebDriver webDriver) {
        try {
            // 创建等待页面加载对象
            WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
            // 等待 document.readyState 为 complete
            wait.until(driver -> ((JavascriptExecutor) driver)
                    .executeScript("return document.readyState").
                    equals("complete")
            );
            // 额外等待一段时间，确保动态内容加载完成
            Thread.sleep(2000);
            log.info("页面加载完成");
        } catch (Exception e) {
            log.error("等待页面加载时出现异常，继续执行截图", e);
        }
    }
}

