package com.mendix.oqlmodule.test;

import com.mendix.oqlmodule.test.utils.StringOutputStream;

import oql.implementation.MxCSVWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MxCSVWriterTest {

  OutputStream output;
  MxCSVWriter defaultWriter;

  @BeforeEach
  public void setup() {
    output = new StringOutputStream();
    
    defaultWriter = new MxCSVWriter(new OutputStreamWriter(output),
        ',',
        Optional.of('"'),
        Optional.of('\\'));
  }
  
  String writeAndGetResult(MxCSVWriter writer, List<List<String>> columns) throws IOException {
    columns.forEach(column -> {
      try {
        writer.writeRow(column);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
    writer.close();
    return output.toString();
  }
  
  @Test
  public void testNoSpecialCharacters() throws IOException {
    assertEquals(
        "\"john\",\"doe\"\r\n"
            + "\"mary\",\"moose\"\r\n",
        writeAndGetResult(defaultWriter,
            List.of(List.of("john","doe"),
                List.of("mary", "moose"))));
  }

  @Test
  public void testQuotedSpecialCharacters() throws IOException {
    List<List<String>> toWrite = List.of(List.of(",","\r\n"),
        List.of("\\", "\""));
    assertEquals(
        "\",\",\"\r\n\"\r\n"
            + "\"\\\",\"\"\"\"\r\n",
        writeAndGetResult(defaultWriter, toWrite)
    );
  }

  @Test
  public void testEscapedSpecialCharacters() throws IOException {
    MxCSVWriter writerWithoutQuote = new MxCSVWriter(new OutputStreamWriter(output),
        ',',
        Optional.empty(),
        Optional.of('\\'));
    
    List<List<String>> toWrite = List.of(List.of(",","\r\n"),
        List.of("\\", "\""));
    assertEquals(
        "\\,,\r\\\n\r\n"
            + "\\\\,\"\r\n",
        writeAndGetResult(writerWithoutQuote, toWrite)
    );
  }
}
