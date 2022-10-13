param([String]$localeToCompare,[String]$fileName)


$path = "..\"
$filterRegex = $fileName + ".properties"
$excluded = @("target")
$outputPath = ".\MissingPropertiesGen\$localeToCompare\"
$reportName = $outputPath + "missingkeys_report_" + $localeToCompare + ".txt"

if ( -not ( Test-Path -Path $outputPath))
{
    New-Item -Path $outputPath -ItemType Directory -Force
}

$filesFiltered = Get-ChildItem $path -Exclude $excluded -Filter $filterRegex -File -Recurse
Write-Output "********************Report********************" > $reportName
Write-Output "`r`n" >> $reportName

foreach($file in $filesFiltered)
{
    if ($file.FullName.Contains("target"))
    {
        continue;
    }
    
    Write-Output "`r`nFile: " + $file.FullName 
    Write-Output "`r`nFile: " + $file.FullName >> $reportName

    $fileNameToGen = $outputPath + $file.Directory.FullName.Substring(3) + "\" + $file.BaseName + "_" + $localeToCompare + ".properties"
    $fileNameToGenExists = Test-Path -Path $fileNameToGen -PathType Leaf
    if ($fileNameToGenExists)
    {
        continue;
    }

    New-Item -Path $fileNameToGen -ItemType File -Force
    
    Get-Content -Path $file.FullName > $fileNameToGen
    
}

Write-Output "`r`n" >> $reportName
Write-Output "********************EOF*************************" >> $reportName