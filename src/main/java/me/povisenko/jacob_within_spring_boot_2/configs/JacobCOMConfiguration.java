package me.povisenko.jacob_within_spring_boot_2.configs;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.LibraryLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.*;

@Profile({"local_windows"})
@Slf4j
@Configuration
public class JacobCOMConfiguration {

    private static final String COM_INTERFACE_NAME = "NAME_OF_COM_INTERFACE_AS_IN_REGISTRY";

    private static final String JACOB_LIB_PATH = System.getProperty("user.dir") + "\\libs\\jacob-1.19";
    private static final String LIB_FILE = System.getProperty("os.arch")
                                                 .equals("amd64") ? "\\jacob-1.19-x64.dll" : "\\jacob-1.19-x86.dll";

    private File temporaryDll;

    static {
        log.info("JACOB lib path: {}", JACOB_LIB_PATH);
        log.info("JACOB file lib path: {}", JACOB_LIB_PATH + LIB_FILE);
        System.setProperty("java.library.path", JACOB_LIB_PATH);
        System.setProperty("com.jacob.debug", "true");
    }

    @PostConstruct
    public void init() throws IOException {
        InputStream inputStream = new FileInputStream(JACOB_LIB_PATH + LIB_FILE);

        temporaryDll = File.createTempFile("jacob", ".dll");
        FileOutputStream outputStream = new FileOutputStream(temporaryDll);
        byte[] array = new byte[8192];
        for (int i = inputStream.read(array); i != -1; i = inputStream.read(array)) {
            outputStream.write(array, 0, i);
        }
        outputStream.close();

        System.setProperty(LibraryLoader.JACOB_DLL_PATH, temporaryDll.getAbsolutePath());
        LibraryLoader.loadJacobLibrary();
        log.info("JACOB library is loaded and ready to use");
    }

    @Bean
    public ActiveXComponent dllAPI() {
        ActiveXComponent activeXComponent = new ActiveXComponent(COM_INTERFACE_NAME);
        log.info("API COM interface {} wrapped into ActiveXComponent is created and ready to use", COM_INTERFACE_NAME);
        return activeXComponent;
    }

    @PreDestroy
    public void clean() {
        temporaryDll.deleteOnExit();
        log.info("Temporary DLL API library is cleaned on exit");
    }
}