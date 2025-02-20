package oql.implementation.opencsv;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The AbstractCSVWriter was created to prevent duplication of code between the CSVWriter and the
 * CSVParserWriter classes.
 *
 * @since 4.2
 */
public abstract class AbstractCSVWriter implements ICSVWriter {

    protected final Writer writer;
    protected String lineEnd;
    protected ResultSetHelper resultService;
    protected volatile IOException exception;

    /**
     * Constructor to initialize the common values.
     *
     * @param writer  Writer used for output of csv data.
     * @param lineEnd String to append at end of data (either "\n" or "\r\n").
     */
    protected AbstractCSVWriter(Writer writer, String lineEnd) {
        this.writer = writer;
        this.lineEnd = lineEnd;
    }

    @Override
    public void writeAll(Iterable<String[]> allLines, boolean applyQuotesToAll) {
        StringBuilder sb = new StringBuilder(INITIAL_STRING_SIZE);
        try {
            for (String[] line : allLines) {
                writeNext(line, applyQuotesToAll, sb);
                sb.setLength(0);
            }
        } catch (IOException e) {
            exception = e;
        }
    }

    /**
     * Writes the column names.
     *
     * @param rs               ResultSet containing column names.
     * @param applyQuotesToAll Whether all header names should be quoted.
     * @throws SQLException Thrown by {@link ResultSetHelper#getColumnNames(ResultSet)}
     */
    protected void writeColumnNames(ResultSet rs, boolean applyQuotesToAll) throws SQLException {
        writeNext(resultService().getColumnNames(rs), applyQuotesToAll);
    }

    @Override
    public int writeAll(ResultSet rs, boolean includeColumnNames, boolean trim, boolean applyQuotesToAll) throws SQLException, IOException {
        int linesWritten = 0;

        if (includeColumnNames) {
            writeColumnNames(rs, applyQuotesToAll);
            linesWritten++;
        }

        while (rs.next()) {
            writeNext(resultService().getColumnValues(rs, trim), applyQuotesToAll);
            linesWritten++;
        }

        return linesWritten;
    }

    @Override
    public void writeNext(String[] nextLine, boolean applyQuotesToAll) {
        try {
            writeNext(nextLine, applyQuotesToAll, new StringBuilder(INITIAL_STRING_SIZE));
        } catch (IOException e) {
            exception = e;
        }
    }

    /**
     * Writes the next line to the file.  This method is a fail-fast method that will throw the
     * IOException of the writer supplied to the CSVWriter (if the Writer does not handle the exceptions itself like
     * the PrintWriter class).
     *
     * @param nextLine         a string array with each comma-separated element as a separate
     *                         entry.
     * @param applyQuotesToAll true if all values are to be quoted.  false applies quotes only
     *                         to values which contain the separator, escape, quote or new line characters.
     * @param appendable       Appendable used as buffer.
     * @throws IOException Exceptions thrown by the writer supplied to CSVWriter.
     */
    protected abstract void writeNext(String[] nextLine, boolean applyQuotesToAll, Appendable appendable) throws IOException;

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        flush();
        writer.close();
    }

    @Override
    public boolean checkError() {

        if (writer instanceof PrintWriter) {
            PrintWriter pw = (PrintWriter) writer;
            return pw.checkError();
        }
        if (exception != null) {  // we don't want to lose the original exception
            flushQuietly();  // checkError in the PrintWriter class flushes the buffer so we shall too.
        } else {
            try {
                flush();
            } catch (IOException ioe) {
                exception = ioe;
            }
        }
        return exception != null;
    }

    @Override
    public IOException getException() {
        return exception;
    }

    @Override
    public void resetError() {
        exception = null;
    }

    @Override
    public void setResultService(ResultSetHelper resultService) {
        this.resultService = resultService;
    }

    /**
     * Lazy resultSetHelper creation.
     *
     * @return Instance of resultSetHelper
     */
    protected ResultSetHelper resultService() {
        if (resultService == null) {
            resultService = new ResultSetHelperService();
        }
        return resultService;
    }
}
