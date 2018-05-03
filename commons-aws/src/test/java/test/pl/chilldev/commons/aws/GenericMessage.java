/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2018 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.aws;

import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenericMessage
{
    private List<UUID> ids;
}
