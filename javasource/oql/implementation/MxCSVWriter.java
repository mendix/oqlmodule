package oql.implementation;

//Based on RFC 4180 and added support for an escape character if quotes are not used

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MxCSVWriter implements Closeable {

  protected Writer writer;
  protected List<Character> specialCharactersToEscape;
  protected char separatorChar;
  protected Optional<Character> quoteChar;
  protected Optional<Character> escapeChar;
  protected String lineEnd = "\r\n";

  public MxCSVWriter(Writer writer, char separatorChar, Optional<Character> quoteChar, Optional<Character> escapeChar) {
    this.writer = writer;
    this.separatorChar = separatorChar;
    this.quoteChar = quoteChar;
    this.escapeChar = escapeChar;
    specialCharactersToEscape = defineSpecialCharactersToEscape();
  }

  public void writeRow(List<String> columns) throws IOException {
    StringBuilder rowBuilder = new StringBuilder(1024);
    
    for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {

      if (columnIndex != 0) {
        rowBuilder.append(separatorChar);
      }

      String nextValue = columns.get(columnIndex);

      quoteChar.ifPresent(rowBuilder::append);
      writeValue(rowBuilder, nextValue);
      quoteChar.ifPresent(rowBuilder::append);
    }

    rowBuilder.append(lineEnd);
    writer.write(rowBuilder.toString());
  }
  
  private void writeValue(StringBuilder rowBuilder, String value) {
    value
        .chars()
        .forEach(character -> writeChar(rowBuilder, character));
  }
  
  private void writeChar(StringBuilder rowBuilder, int charAsInteger) {
    Character character = (char)charAsInteger;
    //Only use escapeChar if the value is not escaped by quoting
    if (quoteChar.isEmpty() && escapeChar.isPresent() && specialCharactersToEscape.stream().anyMatch(specialChar -> specialChar == character)) {
        rowBuilder.append(escapeChar.get().charValue());
    }
    if (quoteChar.stream().anyMatch(specialChar -> specialChar == character)) {
      rowBuilder.append(quoteChar.get().charValue());
    }
    rowBuilder.append(character.charValue());
  }
  
  private List<Character> defineSpecialCharactersToEscape() {
    return Stream.concat(Stream.of( separatorChar, '\n'), escapeChar.stream())
      .collect(Collectors.toList());
  }

  public void close() throws IOException {
    writer.flush();
    writer.close();
  }
}
