import java.util.*;


public class MockCrawler extends Crawler{

  public MockCrawler(String [] foundPages, String [] skippedPages){
    super();
    for(String page : foundPages){
      this.foundPages.add(page);
    }
    for(String page : skippedPages){
      this.skippedPages.add(page);
    }
  }
  public void crawl(String page){
    System.out.println("Ignoring request to crawl: "+page);
  }

}
