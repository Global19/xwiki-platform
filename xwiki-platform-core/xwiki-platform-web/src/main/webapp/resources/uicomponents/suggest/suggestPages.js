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
require.config({
  paths: {
    'xwiki-selectize': "$xwiki.getSkinFile('uicomponents/suggest/xwiki.selectize.js', true)" +
      "?v=$escapetool.url($xwiki.version)"
  }
});

define('xwiki-suggestPages', ['jquery', 'xwiki-selectize'], function($) {
  'use strict';

  var pageIcon = $jsontool.serialize($services.icon.getMetaData('page_white'));
  var webHome = "$!services.model.getEntityReference('DOCUMENT', 'default').name" || 'WebHome';

  var getSelectizeOptions = function(select) {
    // Where to look for pages. The following is supported:
    // * "wiki:wikiName" look for pages in the specified wiki
    // * "space:spaceReference": look for pages in the specified space
    var searchScope = select.data('searchScope') || 'wiki:' + XWiki.currentWiki;
    return {
      create: true,
      load: function(text, callback) {
        loadPages(text, searchScope).done(function(data) {
          var pages = [];
          data.searchResults.forEach(function (element) {
            var hierarchy = element.hierarchy.items;
            var label = hierarchy.pop().label;
            if (element.pageName === webHome) {
              label = hierarchy.pop().label;
            }
            var hint = hierarchy
              .filter(function(element) { return element.type === 'space'; })
              .map(function(element) { return element.label; })
              .join(' / ');
            var doc = new XWiki.Document(XWiki.Model.resolve(element.id, XWiki.EntityType.DOCUMENT))
            var url = doc.getURL();
            pages.push({
              'value': element.pageFullName,
              'label': label,
              'icon': pageIcon,
              'url': url,
              'hint': hint
            });
          });
          callback(pages);
        }).fail(callback);
      }
    }
  };

  var loadPages = function(text, searchScope) {
    var scopes = ['name', 'title'];
    return $.getJSON(getRestSearchURL(searchScope), $.param({
      'q': text,
      'scope': scopes,
      'number': 10,
      'localeAware': true,
      'media': 'json'
    }, true));
  };

  var getRestSearchURL = function(searchScope) {
    var entityReference = resolveEntityReference(searchScope);
    var spaces = entityReference.getReversedReferenceChain().filter(function(component) {
      return component.type === XWiki.EntityType.SPACE;
    }).map(function(component) {
      return component.name;
    });
    var wiki = entityReference.extractReferenceValue(XWiki.EntityType.WIKI);
    return XWiki.Document.getRestSearchURL('', spaces, wiki);
  };

  /**
   * Resolves an entity reference from a string representation of the form "entityType:entityReference".
   */
  var resolveEntityReference = function(typeAndReference) {
    if (typeof typeAndReference === 'string') {
      try {
        return XWiki.Model.resolve(typeAndReference, null, XWiki.currentDocument.documentReference);
      } catch (e) {
        return null;
      }
    }
    return typeAndReference;
  };

  $.fn.suggestPages = function() {
    return this.each(function() {
      $(this).xwikiSelectize(getSelectizeOptions($(this)));
    });
  };
});

require(['jquery', 'xwiki-suggestPages', 'xwiki-events-bridge'], function($) {
  var init = function(event, data) {
    var container = $((data && data.elements) || document);
    container.find('.suggest-pages').suggestPages();
  };

  $(document).on('xwiki:dom:loaded xwiki:dom:updated', init);
  XWiki.domIsLoaded && init();
});
