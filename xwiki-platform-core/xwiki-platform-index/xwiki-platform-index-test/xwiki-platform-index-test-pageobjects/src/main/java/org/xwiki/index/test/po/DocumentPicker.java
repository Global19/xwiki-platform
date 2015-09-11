/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.index.test.po;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.xwiki.test.ui.po.BaseElement;
import org.xwiki.test.ui.po.BreadcrumbElement;

/**
 * Represents the Document Picker.
 *
 * @version $Id$
 * @since 7.2M3
 */
public class DocumentPicker extends BaseElement
{
    protected WebElement container;

    public DocumentPicker(WebElement container)
    {
        this.container = container;
    }

    public String getTitle()
    {
        return getTitleInput().getAttribute("value");
    }

    public DocumentPicker setTitle(String title)
    {
        WebElement titleInput = getTitleInput();
        titleInput.clear();
        titleInput.sendKeys(title);
        return this;
    }

    public WebElement getTitleInput()
    {
        return this.container.findElement(By.className("location-title-field"));
    }

    public BreadcrumbElement getLocation()
    {
        return new BreadcrumbElement(this.container.findElement(By.className("breadcrumb")));
    }

    public DocumentPicker toggleLocationAdvancedEdit()
    {
        this.container.findElement(By.className("location-action-edit")).click();
        return this;
    }

    public String getParent()
    {
        return getParentInput().getAttribute("value");
    }

    public DocumentPicker setParent(String parent)
    {
        WebElement parentInput = getParentInput();
        parentInput.clear();
        parentInput.sendKeys(parent);
        return this;
    }

    public WebElement getParentInput()
    {
        return this.container.findElement(By.className("location-parent-field"));
    }

    public String getName()
    {
        return getNameInput().getAttribute("value");
    }

    public DocumentPicker setName(String name)
    {
        WebElement nameInput = getNameInput();
        nameInput.clear();
        nameInput.sendKeys(name);
        return this;
    }

    public WebElement getNameInput()
    {
        return this.container.findElement(By.className("location-name-field"));
    }

    public DocumentPicker selectDocument(String... path)
    {
        browseDocuments().selectDocument(path);
        return this;
    }

    public DocumentPickerModal browseDocuments()
    {
        this.container.findElement(By.className("location-action-pick")).click();
        return new DocumentPickerModal(this.container.findElement(By.cssSelector(".location-tree.modal"))).waitForIt();
    }

    /**
     * Wait for the Breadcrumb to display the passed path string and throw an exception if the timeout is reached. Note
     * that we need to wait since the Breadcrumb is udated live and asserting its content without waiting would lead to
     * false positives.
     * <p/>
     * Note: This method can not be implemented inside {@link BreadcrumbElement} because a change of parent replaces
     * completely the {@link BreadcrumbElement}'s container and thus it becomes stale. To avoid that, at each wait
     * iteration, we lookup the current breadcrumb element and not a cached one.
     *
     * @param expectedPathString the path string to wait for
     * @since 7.2RC1
     */
    public void waitForLocation(final String expectedPathString)
    {
        // TODO: Ugly hack. Would need to find a better solution
        final StringBuilder currentValue = new StringBuilder();

        try {
            getDriver().waitUntilCondition(new ExpectedCondition<Boolean>()
            {
                @Override
                public Boolean apply(WebDriver driver)
                {
                    try {
                        String value = getLocation().getPathAsString();

                        currentValue.setLength(0);
                        currentValue.append(value);

                        return expectedPathString.equals(value);
                    } catch (Exception e) {
                        return false;
                    }
                }
            });
        } catch (WebDriverException e) {
            // Display a nicer error message than would be displayed otherwise
            throw new WebDriverException(String.format("Found [%s], was expecting [%s]", currentValue.toString(),
                expectedPathString), e);
        }
    }

    /**
     * Wait for the Breadcrumb to display the passed path and throw an exception if the timeout is reached. Note that we
     * need to wait since the Breadcrumb is udated live and asserting its content without waiting would lead to false
     * positives.
     * <p/>
     * Note: This method can not be implemented inside {@link BreadcrumbElement} because a change of parent replaces
     * completely the {@link BreadcrumbElement}'s container and thus it becomes stale. To avoid that, at each wait
     * iteration, we lookup the current breadcrumb element and not a cached one.
     *
     * @param expectedPath the path to wait for
     * @since 7.2RC1
     */
    public void waitForLocation(final List<String> expectedPath)
    {
        // TODO: Ugly hack. Would need to find a better solution
        final List<String> currentPath = new ArrayList<String>();

        try {
            getDriver().waitUntilCondition(new ExpectedCondition<Boolean>()
            {
                @Override
                public Boolean apply(WebDriver driver)
                {
                    try {
                        List<String> path = getLocation().getPath();

                        currentPath.clear();
                        currentPath.addAll(path);

                        return expectedPath.equals(path);
                    } catch (Exception e) {
                        return false;
                    }
                }
            });
        } catch (WebDriverException e) {
            // Display a nicer error message than would be displayed otherwise
            throw new WebDriverException(String.format("Found %s, was expecting %s", currentPath, expectedPath), e);
        }
    }
}
