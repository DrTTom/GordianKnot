package de.tautenhahn.spelling;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import org.junit.Test;


/**
 * Finds typos in different kind of text files, possibly of mixed language, using a black list.
 * This will not ensure correct spelling but may find some typos before others do. Contrary to spell checkers 
 * this test explicitly targets expressions which are not meant to be a text in natural language.
 * 
 */
public class TestSpelling
{
  private static final String STOP_WORD = "NO-SPELLCHECK";
  private static final List<String> BLACKLIST;
  
  private static final List<String> ALLOWED_PHRASES= Arrays.asList("Sass");
  
  static 
  {
    List<String> typos = new ArrayList<>();
    readFile("/typos_de.list", typos);
    readFile("/typos.list", typos);
    BLACKLIST=Collections.unmodifiableList(typos);
  }


  private static void readFile(String name, List<String> typos)
  {
    try(InputStream ins =TestSpelling.class.getResourceAsStream(name); Scanner s = new Scanner(ins, StandardCharsets.UTF_8))
    {
      while (s.hasNext())
      {
        typos.add(s.next());
      }
    }
    catch (IOException e)
    {
      fail(e.getMessage());
    }
  }
  
  private static int LINE_LIMIT=300;

  /**
   * Call the test for all suitable files 
   * @throws IOException 
   */
  private void checkAllFiles(Path baseDir) throws IOException
  {
    Files.walk(baseDir).filter(this::toBeChecked).filter(p-> !p.toFile().isDirectory()).forEach(this::applyBlacklist);
  }
  
  
  private boolean toBeChecked(Path p)
  {
    String fullName = p.toString();
    List<String> extensions= Arrays.asList(".java", ".txt", ".properties", ".jsf", ".xml", ".json", ".html", ".htm", ".xhtml", ".js");
    List<String> ignored = Arrays.asList("/build/", "/node_modules/");
    return extensions.stream().anyMatch(s-> fullName.endsWith(s)) && ignored.stream().noneMatch(n-> fullName.contains(n));
  }
  
  private void applyBlacklist(Path p) 
  {
    try(Scanner s= new Scanner(p, StandardCharsets.UTF_8))
    {
      int i=0;
      while (s.hasNext())
      {
        String line = s.nextLine();
        i++;
         if (line.length()>LINE_LIMIT) 
         {
           continue;
         }
         if (line.contains(STOP_WORD))
         {
           break;
         }
         String lower = line.toLowerCase(Locale.GERMAN); // for English texts OK as well.
         for (String typo: BLACKLIST)
          {
          assertNotPresent(typo, line, lower,  p, i);
          }
      }
    }
    catch (IOException e)
    {
     fail(e.getMessage());
    }
  }

  private void assertNotPresent(String typo, String line, String lowerLine, Path p, int i)
  {
    int pos=lowerLine.indexOf(typo);
    if (pos!=-1)
    {
      int next = pos+typo.length();      
      char prev = pos>0? line.charAt(pos-1): ' ';
      char following = next<line.length()? line.charAt(next):' ';
      if ((!Character.isLetter(following) || (Character.isUpperCase(following) && Character.isLowerCase(line.charAt(next-1))))
        && (!Character.isLetter(prev) || (Character.isLowerCase(prev) && Character.isUpperCase(line.charAt(pos)))))
        {
          for (String allowed:ALLOWED_PHRASES)
          {
            int apos = line.indexOf(allowed);
            if (apos!=-1 && apos<=pos && apos+allowed.length()>=pos+typo.length())
            {
              return;
            }
          }
        fail("Typo '"+typo+"' in "+p+", line "+i+": "+line);
        }
    }
  }


  /**
   * Check for typos before they become embarrassing.
   * @throws IOException
   */
  @Test
  public void test() throws IOException
  {
    checkAllFiles(Paths.get("."));
  }

}
