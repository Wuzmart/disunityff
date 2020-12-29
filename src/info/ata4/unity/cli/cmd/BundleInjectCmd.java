/*
 ** 2014 April 20
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.cmd;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.beust.jcommander.Parameters;

import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.io.util.PathUtils;
import info.ata4.log.LogUtils;
import info.ata4.unity.asset.bundle.AssetBundle;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(commandNames = "bundle-inject", commandDescription = "Injects extracted files back into asset bundles.")
public class BundleInjectCmd extends AssetCommand {

    private static final Logger L = LogUtils.getLogger();

    public BundleInjectCmd() {
        this.setProcessAssets(false);
        this.setProcessBundledAssets(false);
    }

    @Override
    public void processAssetBundle(AssetBundle bundle) throws IOException {
        Path bundleFile = bundle.getSourceFile();
        Path bundleDir = PathUtils.removeExtension(bundleFile);

        // there's no point in injection if the files haven't been extracted yet
        if (Files.notExists(bundleDir)) {
            L.log(Level.WARNING, "Bundle directory {0} doesn''t exist!",
                    bundleDir);
            return;
        }

        String[] list = bundleDir.toFile().list();
        Map<String, ByteBuffer> entries = bundle.getEntries();
        for (String entryName : list) {
            Path entryFile = bundleDir.resolve(entryName);
            if (Files.exists(entryFile)) {
                if (entries.containsKey(entryName)) {
                    L.log(Level.INFO, "Replacing {0}", entryName);
                    entries.replace(entryName,
                            ByteBufferUtils.openReadOnly(entryFile));
                } else {
                    L.log(Level.INFO, "Inserting {0}", entryName);
                    entries.put(entryName,
                            ByteBufferUtils.openReadOnly(entryFile));
                }
            }
        }

        // create backup by renaming the original file
        Path bundleFileBackup = PathUtils.append(bundleFile, ".bak");
        Files.move(bundleFile, bundleFileBackup,
                StandardCopyOption.REPLACE_EXISTING);

        // save bundle to original path
        bundle.save(bundleFile);
    }
}
