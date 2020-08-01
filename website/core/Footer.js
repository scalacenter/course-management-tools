/**
 * Copyright (c) 2017-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

const React = require('react');

class Footer extends React.Component {
  docUrl(doc, language) {
    const baseUrl = this.props.config.baseUrl;
    const docsUrl = this.props.config.docsUrl;
    const docsPart = `${docsUrl ? `${docsUrl}/` : ''}`;
    const langPart = `${language ? `${language}/` : ''}`;
    return `${baseUrl}${docsPart}${langPart}${doc}`;
  }

  pageUrl(doc, language) {
    const baseUrl = this.props.config.baseUrl;
    return baseUrl + (language ? `${language}/` : '') + doc;
  }

  render() {
    return (
      <footer className="nav-footer" id="footer">
        <section className="sitemap">
          <a href={this.props.config.baseUrl} className="nav-home">
            {this.props.config.footerIcon && (
              <img
                src={this.props.config.baseUrl + this.props.config.footerIcon}
                alt={this.props.config.title}
                width="66"
                height="58"
              />
            )}
          </a>
          <div>
            <h5>Docs</h5>
            <a href={this.docUrl('getting_started', this.props.language)}>
              Getting Started
            </a>
            <a href={this.docUrl('reference-intro', this.props.language)}>
              Reference
            </a>
          </div>
          <div>
            <h5>Community</h5>
            <a href={`${this.props.config.repoUrl}/issues`} target="_blank">Open an issue or ask questions</a>
          </div>
          <div>
            <h5>More</h5>
            <a href={`${this.props.config.baseUrl}blog`}>Blog</a>
            <a href={`${this.props.config.repoUrl}`} target="_blank">GitHub</a>
            <a
              className="github-button"
              href={this.props.config.repoUrl}
              data-icon="octicon-star"
              data-count-href="/facebook/docusaurus/stargazers"
              data-show-count="true"
              data-count-aria-label="# stargazers on GitHub"
              aria-label="Star this project on GitHub">
              Star
            </a>
          </div>
          {this.props.config.jetbrains.enabled && (
              <div>
                <img style={{float: 'left', margin: '5px'}}
                    src={this.props.config.baseUrl + this.props.config.jetbrains.icon}
                    alt={this.props.config.title}
                    width="66"
                    height="58"
                />
                <div>
                  <a href={this.props.config.jetbrains.targetUrl} target="_blank">
                    JetBrains supports this project by providing core project contributors with a set of best-in-class
                    developer tools free of charge.
                  </a>
                </div>
              </div>
          )}
        </section>

        <section className="copyright">{this.props.config.copyright}</section>
      </footer>
    );
  }
}

module.exports = Footer;
