param([String]$localeToCompare)

function Format-Hashtable {
    param(
      [Parameter(Mandatory,ValueFromPipeline)]
      [hashtable]$Hashtable,

      [ValidateNotNullOrEmpty()]
      [string]$KeyHeader = 'Name',

      [ValidateNotNullOrEmpty()]
      [string]$ValueHeader = 'Value'
    )

    $Hashtable.GetEnumerator() |Select-Object @{Label=$KeyHeader;Expression={$_.Key}},@{Label=$ValueHeader;Expression={$_.Value}}

}



$path = "..\"
$filterRegex = "*.properties"
$excluded = @("*_*.properties" , "build*.properties", "target")
$outputPath = ".\MissingProperties\$localeToCompare\"
$reportName = $outputPath + "missingkeys_report_" + $localeToCompare + ".txt"

if ( -not ( Test-Path -Path $outputPath))
{
    New-Item -Path $outputPath -ItemType Directory -Force
}

$filesFiltered = Get-ChildItem -Path $path -Exclude $excluded -Filter $filterRegex -File -Recurse
Write-Output "********************Report********************" > $reportName
Write-Output "`r`n" >> $reportName


foreach($file in $filesFiltered)
{
    if ($file.FullName.Contains("target"))
    {
        continue;
    }
    $containsMissingKeys = $false
    $translatedFilesPath = $file.PSParentPath + "\" + $file.BaseName + "_*.properties"
    $translationExisits = Test-Path -Path $translatedFilesPath -PathType Leaf
    [hashtable]$missingProperties = @{}
    [hashTable]$missingPropertyFile = @{}
    if ($translationExisits)
    {
        $filesToFilter = $file.BaseName + "_" + $localeToCompare + ".properties"
        if ($localeToCompare -eq $null)
        {
            $filesToFilter = $file.BaseName + "_*.properties"
        }
        
        $subFiles = Get-ChildItem -Path $file.PSParentPath -Filter $filesToFilter
        
        [hashtable]$propertiesInMain = @{}
        $prevKey = ""
        $prevLine = ""
        foreach($line in Get-Content -Path $file.FullName)
        {
            if ($prevLine -eq "")
            {
                $prevLine = $line
            }

            if ($line.Contains("="))
            {
                $key = $line.Split('=')[0]
                if ($key.Contains("'") -or $key.Contains("$")) #it is a value not a property now
                {
                    if ($prevKey -ne "" -and $prevLine[0] -ne '#')
                    {
                        $propertiesInMain["$prevKey"] = $propertiesInMain["$prevKey"] + "`r`n" + $line
                    }

                    continue
                }

                $prevKey = $key
                $value = $line.Split('=')[1]
                $propertiesInMain["$key"] = $value
            }
            else
            {
                #Check to see if the previous line was not a comment
                if ($prevKey -ne "" -and $prevLine[0] -ne '#')
                {
                    $propertiesInMain["$prevKey"] = $propertiesInMain["$prevKey"] + "`r`n" + $line
                }
            }
            $prevLine = $line
        }

        foreach($subFile in $subFiles)
        {
            $containsMissingKeysSubFile = $false
            [hashtable]$propertiesInSubFile = @{}
            $prevKey = ""
            $prevLine = ""
            foreach($line in Get-Content -Path $subFile.FullName)
            {
                if ($prevLine -eq "")
                {
                    $prevLine = $line
                }
                if ($line.Contains("="))
                {
                    $key = $line.Split('=')[0]
                    if ($key.Contains("'") -or $key.Contains("$"))
                    {
                        if ($prevKey -ne "" -and $prevLine[0] -ne '#')
                        {
                            $propertiesInSubFile["$prevKey"] = $propertiesInSubFile["$prevKey"] + "`r`n" + $line
                        }
                        continue
                    }
                    $value = $line.Split('=')[1]
                    $propertiesInSubFile["$key"] = $value
                }
                else
                {
                    #Check to see if the previous line was not a comment
                    if ($prevKey -ne "" -and $prevLine[0] -ne '#')
                    {
                        $propertiesInSubFile["$prevKey"] = $propertiesInSubFile["$prevKey"] + "`r`n" + $line
                    }
                }
                $prevLine = $line
            }
            foreach($mainKey in $propertiesInMain.Keys)
            {
                if ($propertiesInSubFile.ContainsKey($mainKey))
                {
                    continue;
                }
                $containsMissingKeys = $true
                $containsMissingKeysSubFile = $true
                $missingProperties["$mainKey"] = $propertiesInMain["$mainKey"]
                $locale = $subFile.BaseName.Split("_")[1]
                if ($missingPropertyFile.ContainsKey($mainKey))
                {
                    $missingPropertyFile["$mainKey"] = $missingPropertyFile["$mainKey"] + "," + $locale
                }
                else
                {
                    $missingPropertyFile["$mainKey"] = $locale
                }
            }
            $propertiesToOutputInFile = ""
            foreach($missingPropertyKey in $missingProperties.Keys)
            {
                $propertiesToOutputInFile += $missingPropertyKey + "=" + $missingProperties["$missingPropertyKey"] + "`r`n"
            }
            if ($containsMissingKeysSubFile)
            {
                $pathToCreate = $subFile.DirectoryName -replace [regex]::Escape((pwd).Path), '.'
                $pathToCreate = $pathToCreate.Substring(2)
                $pathToCreate = $outputPath + $pathToCreate
                New-Item -ItemType Directory -Path $pathToCreate -Force
                Copy-Item -Path $subFile.FullName -Destination $pathToCreate
                $fileToAppendTo = $pathToCreate + "\" + $subFile.Name
                Add-Content -Path $fileToAppendTo -Value $propertiesToOutputInFile
            }

        }
        if ($containsMissingKeys)
        {
            Write-Output "--------------------------------------------------------------------------------" >> $reportName
            Write-Output "File:" $file.FullName >> $reportName
            Add-Content -Path .\$reportName -Value "Missing Keys:"
            $missingProperties.Keys | Out-String | Add-Content -Path .\$reportName
            Write-Output "Missing Keys with Properties:" >> $reportName
            $missingProperties | Format-Hashtable -KeyHeader "Property Name" -ValueHeader "Value"| Sort-Object Name | Format-Table -AutoSize | Out-String | Add-Content -Path .\$reportName
            Write-Output "Missing Locale:" >> $reportName
            $missingPropertyFile | Format-Hashtable -KeyHeader "Property Name" -ValueHeader "Missing Locales" | Sort-Object Name | Format-Table -AutoSize | Out-String | Add-Content -Path .\$reportName
            Write-Output "--------------------------------------------------------------------------------" >> $reportName
            Write-Output "`r`n" >> $reportName
        }
        
    }
}

Write-Output "`r`n" >> $reportName
Write-Output "********************EOF*************************" >> $reportName