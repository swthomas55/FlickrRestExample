/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.springsource.samples.resttemplate;

import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.xml.transform.Source;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.springframework.web.client.RestOperations;
import org.springframework.xml.xpath.NodeMapper;
import org.springframework.xml.xpath.XPathOperations;

public class FlickrClient {

    private final RestOperations restTemplate;

    private final XPathOperations xpathTemplate;

    public FlickrClient(RestOperations restTemplate, XPathOperations xpathTemplate) {
        this.restTemplate = restTemplate;
        this.xpathTemplate = xpathTemplate;
    }

    public void doIt(String apiKey, String searchTerm) {
        List<BufferedImage> photos = searchPhotos(apiKey, searchTerm);
        showPhotos(searchTerm, photos);
    }

    @SuppressWarnings("unchecked")
    private List<BufferedImage> searchPhotos(String apiKey, String searchTerm) {
        String photoSearchUrl =
                "http://www.flickr.com/services/rest?method=flickr.photos.search&api+key={api-key}&tags={tag}&per_page=10";
        Source photos = restTemplate.getForObject(photoSearchUrl, Source.class, apiKey, searchTerm);

        final String photoUrl = "http://static.flickr.com/{server}/{id}_{secret}_m.jpg";
        return (List<BufferedImage>) xpathTemplate.evaluate("//photo", photos, new NodeMapper() {
            public Object mapNode(Node node, int i) throws DOMException {
                Element photo = (Element) node;

                Map<String, String> variables = new HashMap<String, String>(3);
                variables.put("server", photo.getAttribute("server"));
                variables.put("id", photo.getAttribute("id"));
                variables.put("secret", photo.getAttribute("secret"));

                return restTemplate.getForObject(photoUrl, BufferedImage.class, variables);
            }
        });
    }

    private void showPhotos(String searchTerm, List<BufferedImage> imageList) {
        JFrame frame = new JFrame(searchTerm + " photos");
        frame.setLayout(new GridLayout(2, imageList.size() / 2));
        for (BufferedImage image : imageList) {
            frame.add(new JLabel(new ImageIcon(image)));
        }
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}
