/**
 * Copyright (c) 2009 julien
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.superfeedr.extension.notification;

import java.util.Date;

public class EntryExtension extends DefaultSuperfeerExtension {
	private String id;
	private String link;
	private String linkType;
	private Date published;
    private Date updated;
	private String summary;
	private String title;
    private String content;

	public EntryExtension(final String id2, final String link2, final String linkType2, final Date published2, final Date updated2,
                          final String title2, final String summary2, final String content2) {
		this.id = id2;
		this.link = link2;
		this.linkType = linkType2;
		this.published = published2;
		this.title = title2;
		this.summary = summary2;
        this.content = content2;
        this.updated = updated2;
	}

	/**
	 * The unique Id of this entry
	 * 
	 * @return a String that represents the id of this entry
	 */
	public String getId() {
		return id;
	}

	/**
	 * The link to the original information this entry represents
	 * 
	 * @return a String that links to the original information
	 */
	public String getLink() {
		return link;
	}

	/**
	 * The Mime typ of the link to the original information this entry
	 * represents
	 */
	public String getLinkType() {
		return linkType;
	}

	/**
	 * The date the information has been published
	 * 
	 * @return the published date
	 */
	public Date getPublished() {
		return published;
	}

	/**
	 * The Summary of this entry
	 * 
	 * @return the summary
	 */
	public String getSummary() {
		return summary;
	}

	/**
	 * The title of this entry
	 * 
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

    /**
     * The content of this entry
     *
     * @return the content
     */
    public String getContent() {
        return content;
    }
}
