/**
 * Copyright (c) 2017-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

const React = require('react');

const CompLibrary = require('../../core/CompLibrary.js');

const MarkdownBlock = CompLibrary.MarkdownBlock; /* Used to read markdown */
const Container = CompLibrary.Container;
const GridBlock = CompLibrary.GridBlock;

class HomeSplash extends React.Component {
  render() {
    const {siteConfig, language = ''} = this.props;
    const {baseUrl, docsUrl} = siteConfig;
    const docsPart = `${docsUrl ? `${docsUrl}/` : ''}`;
    const langPart = `${language ? `${language}/` : ''}`;
    const docUrl = doc => `${baseUrl}${docsPart}${langPart}${doc}`;

    const SplashContainer = props => (
      <div className="homeContainer">
        <div className="homeSplashFade">
          <div className="wrapper homeWrapper">{props.children}</div>
        </div>
      </div>
    );

    const Logo = props => (
      <div className="projectLogo">
        <img src={props.img_src} alt="Project Logo" />
      </div>
    );

    const ProjectTitle = props => (
      <h2 className="projectTitle">
        {props.title}
        <small>{props.tagline}</small>
      </h2>
    );

    const PromoSection = props => (
      <div className="section promoSection">
        <div className="promoRow">
          <div className="pluginRowBlock">{props.children}</div>
        </div>
      </div>
    );

    const Button = props => (
      <div className="pluginWrapper buttonWrapper">
        <a className="button" href={props.href} target={props.target}>
          {props.children}
        </a>
      </div>
    );

    return (
      <SplashContainer>
        {/*<Logo img_src={`${baseUrl}img/undraw_online.svg`} />*/}
        <div className="inner">
          <ProjectTitle tagline={siteConfig.tagline} title={siteConfig.title} />
          <PromoSection>
            <Button href="#try">Try It Out</Button>
            <Button href={docUrl('doc1.html')}>Course Example</Button>
            <Button href={docUrl('doc2.html')}>Documentation Example</Button>
          </PromoSection>
        </div>
      </SplashContainer>
    );
  }
}

class Index extends React.Component {
  render() {
    const {config: siteConfig, language = ''} = this.props;
    const {baseUrl} = siteConfig;

    const Block = props => (
      <Container
        padding={['bottom', 'top']}
        id={props.id}
        background={props.background}>
        <GridBlock
          align="center"
          contents={props.children}
          layout={props.layout}
        />
      </Container>
    );

    const FeatureCallout = () => (
      <div
        className="productShowcaseSection paddingBottom"
        style={{textAlign: 'center'}}>
        <h2>Feature Callout</h2>
        <MarkdownBlock>These are features of this project</MarkdownBlock>
      </div>
    );

    const LiveCoding = () => (
      <Block id="try">
        {[
          {
            content:
              'Perform Live Coding without stress. Jump to known states' +
              'and save live changes for later reference',
            image: `${baseUrl}img/undraw_dev_focus_b9xo.svg`,
            imageAlign: 'left',
            title: 'Live Coding with a Safety Net',
          },
        ]}
      </Block>
    );

    const DemosAndPOCs = () => (
      <Block background="light">
        {[
          {
            content:
              'Build and Maintain non-trivial Demos or Proofs of Concept<br>and<br>decompose them in small steps',
            image: `${baseUrl}img/undraw_product_teardown_elol.svg`,
            imageAlign: 'right',
            title: 'Build and Maintain Demos and Proofs Of Concept',
          },
        ]}
      </Block>
    );

    const TrainingCourses = () => (
      <Block background="light">
        {[
          {
            content:
                'Build and maintain training course exercises in a repo and make them ' +
                'available in a classroom setting in a student-friendly format',
            image: `${baseUrl}img/build.svg`,
            imageAlign: 'right',
            title: 'Build and Maintain Training Courses',
          },
        ]}
      </Block>
    );

    const Features = () => (
      <Block layout="fourColumn">
        {[
          {
            content: 'This is the content of my feature',
            image: `${baseUrl}img/undraw_react.svg`,
            imageAlign: 'top',
            title: 'Feature One',
          },
          {
            content: 'The content of my second feature',
            image: `${baseUrl}img/undraw_operating_system.svg`,
            imageAlign: 'top',
            title: 'Feature Two',
          },
        ]}
      </Block>
    );

    const Showcase = () => {
      if ((siteConfig.users || []).length === 0) {
        return null;
      }

      const showcase = siteConfig.users
        .filter(user => user.pinned)
        .map(user => (
          <a href={user.infoLink} key={user.infoLink}>
            <img src={user.image} alt={user.caption} title={user.caption} />
          </a>
        ));

      const pageUrl = page => baseUrl + (language ? `${language}/` : '') + page;

      return (
        <div className="productShowcaseSection paddingBottom">
          <h2>Who is Using This?</h2>
          <p>This project is used by all these projects</p>
          <div className="logos">{showcase}</div>
          {/*<div className="more-users">*/}
          {/*  <a className="button" href={pageUrl('users.html')}>*/}
          {/*    More {siteConfig.title} Users*/}
          {/*  </a>*/}
          {/*</div>*/}
        </div>
      );
    };

    return (
      <div>
        <HomeSplash siteConfig={siteConfig} language={language} />
        <div className="mainContainer">
          {/*<Features />*/}
          {/*<FeatureCallout />*/}
          <TrainingCourses />
          <LiveCoding />
          <DemosAndPOCs />
          <Showcase />
        </div>
      </div>
    );
  }
}

module.exports = Index;
