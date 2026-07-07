package oql.implementation;

//Based on RFC 4180 and added support for an escape character if quotes are not used

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.mendix.systemwideinterfaces.connectionbus.data.IDataRow;
import com.mendix.systemwideinterfaces.connectionbus.data.IDataTable;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixIdentifier;

public class MxCSVWriter implements Closeable {

	private Writer writer;
	private List<Character> specialCharactersToEscape;
	private char separatorChar;
	private boolean removeNewLinesFromValues;
	private Optional<Character> quoteChar;
	private Optional<Character> escapeChar;
	private Optional<Character> initialChar;
	private Optional<Character> endChar;
	private String lineEnd = "\r\n";

	public MxCSVWriter(Writer writer, char separatorChar, Optional<Character> quoteChar, Optional<Character> escapeChar,
			Optional<Character> initialChar, Optional<Character> endChar, boolean removeNewLinesFromValues) {
		this.writer = writer;
		this.separatorChar = separatorChar;
		this.quoteChar = quoteChar;
		this.escapeChar = escapeChar;
		this.initialChar = initialChar;
		this.endChar = endChar;
		this.removeNewLinesFromValues = removeNewLinesFromValues;
		specialCharactersToEscape = defineSpecialCharactersToEscape();
	}

	public MxCSVWriter(Writer writer, char separatorChar, Optional<Character> quoteChar, Optional<Character> escapeChar, boolean removeNewLinesFromValues) {
		this(writer, separatorChar, quoteChar, escapeChar, Optional.empty(), Optional.empty(), removeNewLinesFromValues);
	}

	public void writeDataTable(IDataTable results, IContext context) throws IOException {
		for (IDataRow row : results.getRows()) {
			List<String> values = IntStream
				.range(0, results.getSchema().getColumnCount())
				.mapToObj(index -> row.getValue(context, index))
				.map(value -> {
					if (value == null) return "";
					else {
						if (value instanceof Date) {
							return Long.toString(((Date) value).getTime()); // use timestamp to export for more precision than just seconds.
						} else if (value instanceof IMendixIdentifier) {
							return Long.toString(((IMendixIdentifier) value).toLong());
						} else {
							return value.toString();
						}
					}
				})
				.map(value -> removeNewLinesFromValues ? value.replaceAll("(\r\n|\n|\r)", " ") : value)
				.collect(Collectors.toCollection(ArrayList::new));
			writeRow(values);
		}
	}

	public void writeRow(List<String> columns) throws IOException {
		StringBuilder rowBuilder = new StringBuilder(1024);

		initialChar.ifPresent(rowBuilder::append);

		for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {

      if (columnIndex != 0) {
        rowBuilder.append(separatorChar);
      }

      String nextValue = columns.get(columnIndex);

			quoteChar.ifPresent(rowBuilder::append);
			writeValue(rowBuilder, nextValue);
			quoteChar.ifPresent(rowBuilder::append);
		}
		
		endChar.ifPresent(rowBuilder::append);

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
