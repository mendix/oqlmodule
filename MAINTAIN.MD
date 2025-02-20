# Prepare your development environment

## Install Mendix

First determine the correct version using `./gradlew modelerVersion`.

Windows users can easily go to [this page](https://appstore.home-accp.mendix.com/link/modelers/), find their desired Mendix installation package, download and install it.

## Set environment variables

Make sure the following variables are adjusted properly.

- Mendix installation path variables `MX_INSTALL_PATH` and `MX_INSTALL_VERSION` can be passed either as an environment variable or a java property.
  These two variable are only necessary for the **gradle run** and **gradle test** (**MiniAppLauncher**) task to function properly.

  ```
    + $MX_INSTALL_PATH (e.g. "C:\Program Files\Mendix" or "/home/user/.mendix")
    +---+ $MX_INSTALL_VERSION (e.g. "7.23.7.55882")
        +---+ runtime
            +---+ bundles
                +---+ *.jar
  ```

    - Assign a value to `MX_INSTALL_PATH` only in case your Mendix installation path is different from the default installation path, since it will default to `$PROGRAMFILES/Mendix` on Windows and `$HOME/.mendix` otherwise.

    - As Mendix projects are fully compatible only with the Mendix version they are created with, ensure that the default in `environment.gradle` matches **gradle modelerVersion** and do not set the environment variable.

## Prepare

To prepare the project for runtime to be launched we invoke `mxbuild` using Docker or local install of mendix with the command
`./gradlew mxBuild`.

## Development & running tests

After running mxBuild, tests can be run with `./gradlew test`


## Update dependent Appstore modules

- For the dependent `Community Commons` module, download a new version of the module and replace the existing version in studio pro.
  Make sure to update the dependencies of those module accordingly `gradle.build` file.

## Exporting and publishing

- In StudioPro rename the specific version stub folder inside the project to the correct number (by renaming the `DatabaseReplication/_Docs/_version_x.y.z` folder).
- Run `./gradlew exportModule` to test exporting the module
- Run `./gradlew publishModuleToMarketplace` to publish the module (See [gradle-publish-module](https://gitlab.rnd.mendix.com/runtime/gradle-mx-publish-module-plugin) plugin documentation for details)
- Go to [appstore](https://marketplace.mendix.com/link/component/66876) and verify the release details.