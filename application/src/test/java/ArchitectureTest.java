import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.Test;

public class ArchitectureTest {

  // TODO can get the modules programmatically instead of hard coded

  /**
   * Make sure the non-player modules do not access any classes in the following packages
   * <ul>
   *   <li>player.exception</li>
   *   <li>player.repository</li>
   *   <li>player.service</li>
   * </ul>
   */
  @Test
  public void playerModuleInterfaceAndModel() {
    JavaClasses importedClasses = new ClassFileImporter().importPackages("com.texastoc.module");
    noClasses().that()
        .resideInAnyPackage("..game..", "..notification..", "..season..", "..quarterly..",
            "..settings..", "..clock..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..player.exception..", "..player.repository..", "..player.service..")
        .check(importedClasses);
  }

  /**
   * Make sure the non-settings modules do not access any classes in the following packages
   * <ul>
   *   <li>settings.repository</li>
   *   <li>settings.service</li>
   * </ul>
   */
  @Test
  public void settingsModuleInterfaceAndModel() {
    JavaClasses importedClasses = new ClassFileImporter().importPackages("com.texastoc.module");
    noClasses().that()
        .resideInAnyPackage("..player..", "..game..", "..notification..", "..season..",
            "..quarterly..", "..clock..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..settings.repository..", "..settings.service..")
        .check(importedClasses);
  }

  /**
   * Make sure the non-game modules do not access any classes in the following packages
   * <ul>
   *   <li>game.calculator</li>
   *   <li>game.config</li>
   *   <li>game.connector</li>
   *   <li>game.exception</li>
   *   <li>game.repository</li>
   *   <li>game.request</li>
   *   <li>game.service</li>
   * </ul>
   */
  @Test
  public void gameModuleInterfaceAndModel() {
    JavaClasses importedClasses = new ClassFileImporter().importPackages("com.texastoc.module");
    noClasses().that()
        .resideInAnyPackage("..player..", "..settings..", "..notification..", "..season..",
            "..quarterly..", "..clock..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..game.config..",
            // TODO figure out websocket "..game.connector..",
            "..game.exception..",
            "..game.repository..",
            "..game.service..")
        .check(importedClasses);
  }

  /**
   * Make sure the non-season modules do not access any classes in the following packages
   * <ul>
   *   <li>season.calculator</li>
   *   <li>season.exception</li>
   *   <li>season.repository</li>
   *   <li>season.service</li>
   * </ul>
   */
  @Test
  public void seasonModuleInterfaceAndModel() {
    JavaClasses importedClasses = new ClassFileImporter().importPackages("com.texastoc.module");
    noClasses().that()
        .resideInAnyPackage("..player..", "..settings..", "..notification..", "..game..",
            "..quarterly..", "..clock..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..season.calculator..", "..season.exception..",
            "..season.repository..", "..season.service..")
        .check(importedClasses);
  }

  /**
   * Make sure the non-quarterly season modules do not access any classes in the following packages
   * <ul>
   *   <li>quarterly.calculator</li>
   *   <li>quarterly.exception</li>
   *   <li>quarterly.repository</li>
   *   <li>quarterly.service</li>
   * </ul>
   */
  @Test
  public void quarterlySeasonModuleInterfaceAndModel() {
    JavaClasses importedClasses = new ClassFileImporter().importPackages("com.texastoc.module");
    noClasses().that()
        .resideInAnyPackage("..player..", "..settings..", "..notification..", "..game..",
            "..season..", "..clock..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..quarterly.calculator..", "..quarterly.exception..",
            "..quarterly.repository..", "..quarterly.service..")
        .check(importedClasses);
  }

  @Test
  public void notificationModuleInterfaceAndModel() {
    JavaClasses importedClasses = new ClassFileImporter().importPackages("com.texastoc.module");
    noClasses().that()
        .resideInAnyPackage("..player..", "..settings..", "..game..", "..season..", "..quarterly..",
            "..clock..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..notification.config..", "..notification.exception..",
            "..notification.repository..", "..notification.connector..", "..notification.service..")
        .check(importedClasses);
  }

  @Test
  public void clockModuleInterfaceAndModel() {
    JavaClasses importedClasses = new ClassFileImporter().importPackages("com.texastoc.module");
    noClasses().that()
        .resideInAnyPackage("..player..", "..settings..", "..game..", "..season..", "..quarterly..",
            "..notification..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..clock.config..", "..clock.service..")
        .check(importedClasses);
  }
}
