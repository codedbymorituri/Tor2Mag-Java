/**
 * Copyright (C) 2011-2012 Turn, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Code adapted from the ttorrent project (https://github.com/mpetazzoni/ttorrent)
 * (2019) codedbymorituri
 */

package torrent;

import bencode.BDecoder;
import bencode.BEncodedValue;
import bencode.BEncoder;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.*;

public class TorrentInfo {

    private String hash;
    private String name;
    private List<List<URI>> trackers;
    private Date creationDate;
    private String comment;
    private String createdBy;
    private List<String> files;
    private List<String> fileSizes;
    private long size;
    private int pieceLength;

    private final File torrentFile;

    public TorrentInfo(File torrentFile) throws Exception {
        this.torrentFile = torrentFile;
        parseTorrent();
    }

    private void parseTorrent() throws Exception {
        FileInputStream inputStream = new FileInputStream(torrentFile);
        BDecoder decoder = new BDecoder(inputStream);
        Map<String, BEncodedValue> torrentMap = new HashMap<>(decoder.decodeMap().getMap());
        Map<String, BEncodedValue> infoMap = new HashMap<>(torrentMap.get("info").getMap());
        name = infoMap.get("name").getString();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BEncoder.encode(infoMap, outputStream);
        byte[] encodedInfo = outputStream.toByteArray();
        byte[] infoHash = hash(encodedInfo);
        hash = byteArrayToHexString(infoHash);

        trackers = new ArrayList<>();
        Set<URI> allTrackers = new HashSet<>();
        if (torrentMap.containsKey("announce-list")) {
            List<BEncodedValue> tiers = torrentMap.get("announce-list").getList();
            for (BEncodedValue tierValue : tiers) {
                List<BEncodedValue> tierTrackers = tierValue.getList();
                if (tierTrackers.isEmpty()) {
                    continue;
                }
                List<URI> tier = new ArrayList<>();
                for (BEncodedValue tracker : tierTrackers) {
                    URI uri = new URI(tracker.getString());
                    if (!allTrackers.contains(uri)) {
                        tier.add(uri);
                        allTrackers.add(uri);
                    }
                }
                if (!tier.isEmpty()) {
                    trackers.add(tier);
                }
            }
        } else if (torrentMap.containsKey("announce")) {
            URI tracker = new URI(torrentMap.get("announce").getString());
            allTrackers.add(tracker);
            List<URI> tier = new ArrayList<>();
            tier.add(tracker);
            trackers.add(tier);
        }

        if (torrentMap.containsKey("creation date")) {
            creationDate = new Date(torrentMap.get("creation date").getLong() * 1000);
        } else {
            creationDate = null;
        }

        if (torrentMap.containsKey("comment")) {
            comment = torrentMap.get("comment").getString();
        } else {
            comment = null;
        }

        if (torrentMap.containsKey("created by")) {
            createdBy = torrentMap.get("created by").getString();
        } else {
            createdBy = null;
        }

        files = new ArrayList<>();
        fileSizes = new ArrayList<>();
        long fileSize = 0;
        if (infoMap.containsKey("files")) {
            for (BEncodedValue file : infoMap.get("files").getList()) {
                Map<String, BEncodedValue> fileInfo = file.getMap();
                StringBuilder path = new StringBuilder();
                for (BEncodedValue pathElement : fileInfo.get("path").getList()) {
                    path.append(File.separator).append(pathElement.getString());
                }
                String filePath = path.toString();
                long length = fileInfo.get("length").getLong();
                fileSize = fileSize + length;
                files.add(filePath);
                fileSizes.add(filePath + " (" + formatSize(length) + ")");
            }
        } else {
            fileSize = infoMap.get("length").getLong();
            files.add(name);
            fileSizes.add(name + " (" + formatSize(fileSize) + ")");
        }
        size = fileSize;
        pieceLength = infoMap.get("piece length").getInt();
    }

    private byte[] hash(byte[] bytes) {
        return DigestUtils.sha1(bytes);
    }

    private String byteArrayToHexString(byte[] bytes) {
        return new String(Hex.encodeHex(bytes, false));
    }

    private String formatSize(long size) {
        if (size < 1024) {
            return size + " B";
        }
        int zeros = (63 - Long.numberOfLeadingZeros(size)) / 10;
        return String.format("%.1f %sB", (double)size / (1L << (zeros*10)), " KMGTPE".charAt(zeros));
    }

    public String getName() {
        return name;
    }

    public String getHash() {
        return hash;
    }

    public String getComment() {
        return comment;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getPieces() {
        long pieces = size / pieceLength + 1;
        return pieces + " pieces @ " + formatSize(pieceLength);
    }

    public String getSize() {
        return formatSize(size);
    }

    public long getSizeInBytes() {
        return size;
    }

    public boolean isMultiFile() {
        return files.size() > 1;
    }

    public int getNumberOfFiles() {
        return files.size();
    }

    public List<String> getFileNames() {
        return files;
    }

    public List<String> getFileSizes() {
        return fileSizes;
    }

    public List<String> getTrackers() {
        List<String> trackerList = new ArrayList<>();
        for (List<URI> trackerURI : trackers) {
            for (URI tracker : trackerURI) {
                trackerList.add(tracker.toString());
            }
        }
        return trackerList;
    }

    public String getMagnetLink() {
        return String.format("magnet:?xt=urn:btih:%s&dn=%s", hash, name);
    }

    public String getMagnetLinkWithTrackers() {
        String trackerInfo = "";
        String magnetLink = getMagnetLink();
        List<String> trackers = getTrackers();
        for (String tracker : trackers) {
            trackerInfo = trackerInfo + "&tr=" + tracker;
        }
        return magnetLink + trackerInfo;
    }

    @Override
    public String toString() {
        return name;
    }

}
