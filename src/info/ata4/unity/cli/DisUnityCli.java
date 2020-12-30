/*
 ** 2013 July 05
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli;

import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import info.ata4.log.LogUtils;
import info.ata4.unity.DisUnity;
import info.ata4.unity.cli.cmd.BundleCreateCmd;
import info.ata4.unity.cli.cmd.BundleExtractCmd;
import info.ata4.unity.cli.cmd.BundleInjectCmd;
import info.ata4.unity.cli.cmd.BundleListCmd;
import info.ata4.unity.cli.cmd.Command;
import info.ata4.unity.cli.cmd.DebugDeserializerCmd;
import info.ata4.unity.cli.cmd.DebugStructDBCmd;
import info.ata4.unity.cli.cmd.DumpCmd;
import info.ata4.unity.cli.cmd.DumpStructCmd;
import info.ata4.unity.cli.cmd.ExtractCmd;
import info.ata4.unity.cli.cmd.ExtractRawCmd;
import info.ata4.unity.cli.cmd.ExtractStructCmd;
import info.ata4.unity.cli.cmd.ExtractTxtCmd;
import info.ata4.unity.cli.cmd.InfoCmd;
import info.ata4.unity.cli.cmd.LearnCmd;
import info.ata4.unity.cli.cmd.ListCmd;
import info.ata4.unity.cli.cmd.SplitCmd;
import info.ata4.unity.cli.cmd.StatsCmd;

/**
 * DisUnity command line interface.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DisUnityCli implements Runnable {

    private static final Logger L = LogUtils.getLogger();

    private final DisUnityOptions opts = new DisUnityOptions();
    private final JCommander jc = new JCommander();

    public DisUnityCli() {
        this.jc.setProgramName(DisUnity.getProgramName());
        this.jc.addObject(this.opts);

        PrintStream out = System.out;

        // asset commands
        this.jc.addCommand(new DumpCmd());
        this.jc.addCommand(new DumpStructCmd());
        this.jc.addCommand(new ExtractCmd());
        this.jc.addCommand(new ExtractRawCmd());
        this.jc.addCommand(new ExtractTxtCmd());
        this.jc.addCommand(new ExtractStructCmd());
        this.jc.addCommand(new InfoCmd(out));
        this.jc.addCommand(new StatsCmd(out));
        this.jc.addCommand(new LearnCmd());
        this.jc.addCommand(new ListCmd(out));
        this.jc.addCommand(new SplitCmd());

        // bundle commands
        this.jc.addCommand(new BundleExtractCmd());
        this.jc.addCommand(new BundleInjectCmd());
        this.jc.addCommand(new BundleListCmd(out));
        this.jc.addCommand(new BundleCreateCmd());

        // debug commands
        this.jc.addCommand(new DebugDeserializerCmd());
        this.jc.addCommand(new DebugStructDBCmd());
    }

    public void parse(String[] args) {
        L.info(DisUnity.getSignature());

        this.jc.parse(args);

        // display usage
        if (this.opts.isHelp()) {
            this.jc.usage();
        }

        // increase logging level if requested
        if (this.opts.isVerbose()) {
            LogUtils.configure(Level.ALL);
        }
    }

    @Override
    public void run() {
        if (this.opts.isHelp()) {
            return;
        }

        String cmdName = this.jc.getParsedCommand();
        if (cmdName == null) {
            this.jc.usage();
            return;
        }

        JCommander jcc = this.jc.getCommands().get(cmdName);

        Command cmd = (Command) jcc.getObjects().get(0);
        cmd.setOptions(this.opts);
        cmd.run();
    }

    /**
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        LogUtils.configure();

        DisUnityCli cli = new DisUnityCli();

        try {
            cli.parse(args);
            cli.run();
        } catch (ParameterException ex) {
            L.log(Level.WARNING, "Parameter error: {0}", ex.getMessage());
        } catch (Throwable t) {
            L.log(Level.SEVERE, "Fatal error", t);
        }
    }
}
