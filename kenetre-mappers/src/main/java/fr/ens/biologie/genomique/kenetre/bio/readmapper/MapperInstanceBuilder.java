package fr.ens.biologie.genomique.kenetre.bio.readmapper;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.Objects;

/**
 * This class define a builder for a mapper instance.
 *
 * @author Laurent Jourdren
 * @since 2.6
 */
public class MapperInstanceBuilder {

  private final Mapper mapper;

  private String mapperVersion;
  private String mapperFlavor;

  private boolean useBundledBinaries;

  private String dockerImage;

  /**
   * Set the mapper version.
   *
   * @param mapperVersion the mapperVersion to set
   * @return the MapperBuilderInstance instance
   */
  public MapperInstanceBuilder withMapperVersion(String mapperVersion) {

    this.mapperVersion = mapperVersion;
    return this;
  }

  /**
   * Set the flavor of the mapper.
   *
   * @param mapperFlavor the mapperFlavor to set
   * @return the MapperBuilderInstance instance
   */
  public MapperInstanceBuilder withMapperFlavor(String mapperFlavor) {

    this.mapperFlavor = mapperFlavor;
    return this;
  }

  /**
   * Set if bundled binaries must be used.
   *
   * @param useBundledBinaries the useBundledBinaries to set
   * @return the MapperBuilderInstance instance
   */
  public MapperInstanceBuilder withUseBundledBinaries(boolean useBundledBinaries) {

    this.useBundledBinaries = useBundledBinaries;
    return this;
  }

  /**
   * Set the docker image to use.
   *
   * @param dockerImage the docker image to set
   * @return the MapperBuilderInstance instance
   */
  public MapperInstanceBuilder withDockerImage(String dockerImage) {

    requireNonNull(dockerImage);

    this.dockerImage = dockerImage;
    return this;
  }

  //
  // Build method
  //

  /**
   * Build the mapper instance.
   *
   * @return a mapper instance
   * @throws IOException if an error occurs while creating the mapper instance
   */
  public MapperInstance build() throws IOException {

    return this.mapper.newMapperInstance(
        this.mapperVersion, this.mapperFlavor, this.useBundledBinaries, this.dockerImage);
  }

  //
  // Constructor
  //

  public MapperInstanceBuilder(Mapper mapper) {

    Objects.requireNonNull(mapper);
    this.mapper = mapper;
  }
}
