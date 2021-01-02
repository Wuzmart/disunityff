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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.beust.jcommander.Parameters;

import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.log.LogUtils;
import info.ata4.unity.asset.bundle.AssetBundle;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(commandNames = "bundle-create", commandDescription = "Creates an asset bundle from a template bundle and packs it with the contents of a folder.")
public class BundleCreateCmd extends AssetCommand {

    private static final Logger L = LogUtils.getLogger();

    public BundleCreateCmd() {
        this.setProcessAssets(false);
        this.setProcessBundledAssets(false);
    }

    @Override
    public void processAssetBundle(AssetBundle bundle) throws IOException {
        Path bundleFile = bundle.getSourceFile().toAbsolutePath();

        String[] rootList = bundleFile.getParent().toFile().list();
        for (String subPath : rootList) {
            Path subDir = bundleFile.resolveSibling(subPath);
            if (subDir.toFile().isDirectory()
                    && !subDir.getFileName().toString().equals("out")) {
                System.out.println("Packing " + subDir + "...");

                String[] list = subDir.toFile().list();
                Map<String, ByteBuffer> entries = bundle.getEntries();
                entries.clear(); // clear the existing entries in memory

                // add new entries
                for (String entryName : list) {
                    Path entryFile = subDir.resolve(entryName);
                    if (Files.exists(entryFile)) {
                        L.log(Level.INFO, "Inserting {0}", entryName);
                        entries.put(entryName,
                                ByteBufferUtils.openReadOnly(entryFile));
                    }
                }
                bundle.save(bundleFile.resolveSibling("out")
                        .resolve(subDir.getFileName())); // save with same name as directory
            }
        }
    }
}
